package strawman.collection

import scala.{Boolean, Ordering, `inline`}
import scala.annotation.unchecked.uncheckedVariance

/** Base type of sorted sets */
trait SortedSet[A] extends Set[A] with SortedSetOps[A, SortedSet, SortedSet[A]]

trait SortedSetOps[A, +CC[X] <: SortedSet[X], +C <: SortedSet[A]]
  extends SetOps[A, Set, C]
     with SortedOps[A, CC, C] {

  protected[this] def sortedFromIterable[B: Ordering](it: Iterable[B]): CC[B]

  def firstKey: A = head
  def lastKey: A = last

  override def withFilter(p: A => Boolean): SortedWithFilter = new SortedWithFilter(View.Filter(coll, p))

  /** Specialize `WithFilter` for sorted collections
    */
  class SortedWithFilter(filtered: View[A @uncheckedVariance]) extends WithFilter(filtered) {

    def map[B : Ordering](f: A => B): CC[B] = sortedIterableFactory.sortedFromIterable(View.Map(filtered, f))

    def flatMap[B : Ordering](f: A => IterableOnce[B]): CC[B] = sortedIterableFactory.sortedFromIterable(View.FlatMap(filtered, f))

    override def withFilter(p: A => Boolean): SortedWithFilter =
      new SortedWithFilter(filtered.filter(p))

  }

  /** Map */
  def map[B : Ordering](f: A => B): CC[B] = sortedFromIterable(View.Map(coll, f))

  /** Flatmap */
  def flatMap[B : Ordering](f: A => IterableOnce[B]): CC[B] = sortedFromIterable(View.FlatMap(coll, f))

  /** Zip. Interesting because it requires to align to source collections. */
  def zip[B](xs: IterableOnce[B])(implicit ev: Ordering[(A @uncheckedVariance, B)]): CC[(A @uncheckedVariance, B)] = sortedFromIterable(View.Zip(coll, xs))
  // sound bcs of VarianceNote

  def collect[B: Ordering](pf: scala.PartialFunction[A, B]): CC[B] = flatMap(a =>
    if (pf.isDefinedAt(a)) View.Single(pf(a))
    else View.Empty
  )
}

object SortedSet extends SortedIterableFactory.Delegate[SortedSet](immutable.SortedSet)
