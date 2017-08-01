package patmat

import scala.collection.immutable._


object Huffman {

  abstract class CodeTree
  case class Fork(left: CodeTree, right: CodeTree, chars: List[Char], weight: Int) extends CodeTree
  case class Leaf(char: Char, weight: Int) extends CodeTree




  def weight(tree: CodeTree): Int = tree match {
    case Fork(_,_,_,w) => w
    case Leaf(_,w) => w
  }

  def chars(tree: CodeTree): List[Char] = tree match {
    case Fork(_,_,ch,_) => ch
    case Leaf(c,_) => List(c)
  }

  def makeCodeTree(left: CodeTree, right: CodeTree) =
    Fork(left, right, chars(left) ::: chars(right), weight(left) + weight(right))

  def string2Chars(str: String): List[Char] = str.toList

  def times(chars: List[Char]): List[(Char, Int)] = {
    def increment(acc:Map[Char, Int], c:Char) = {
      val count = (acc get c).getOrElse(0) + 1
      acc + ((c, count))
    }

    (Map[Char,Int]() /: chars)(increment).iterator.toList
  }

  def makeOrderedLeafList(freqs: List[(Char, Int)]): List[Leaf] = {
    freqs.sortWith((f1,f2) => f1._2 < f2._2).map((f) => Leaf (f._1, f._2))
  }


  def singleton(trees: List[CodeTree]): Boolean = trees.size == 1

  def combine(trees: List[CodeTree]): List[CodeTree] = trees match {
    case left :: right :: cs => (makeCodeTree(left, right) :: cs)
      .sortWith((t1, t2) => weight(t1) < weight(t2))
    case _ => trees
  }

  def until(p: List[CodeTree]=>Boolean, f: List[CodeTree]=>List[CodeTree])(trees: List[CodeTree]): List[CodeTree] = {
    if (p(trees)) trees
    else until(p, f)( f(trees) )
  }

  def createCodeTree(chars: List[Char]): CodeTree = until(singleton, combine)( makeOrderedLeafList(times(chars)) ).head



  type Bit = Int

  def decode(tree: CodeTree, bits: List[Bit]): List[Char] = {
    def treeTraverse(remaining: CodeTree, bits: List[Bit]): List[Char] = remaining match {
      case Leaf(c, _) if bits.isEmpty => List(c)
      case Leaf(c, _) => c :: treeTraverse(tree, bits)
      case Fork(left, right, _, _) if bits.head == 0 => treeTraverse(left, bits.tail)
      case Fork(left, right, _, _) => treeTraverse(right, bits.tail)
    }

    treeTraverse(tree, bits)
  }

  val frenchCode: CodeTree = Fork(Fork(Fork(Leaf('s',121895),Fork(Leaf('d',56269),Fork(Fork(Fork(Leaf('x',5928),Leaf('j',8351),List('x','j'),14279),Leaf('f',16351),List('x','j','f'),30630),Fork(Fork(Fork(Fork(Leaf('z',2093),Fork(Leaf('k',745),Leaf('w',1747),List('k','w'),2492),List('z','k','w'),4585),Leaf('y',4725),List('z','k','w','y'),9310),Leaf('h',11298),List('z','k','w','y','h'),20608),Leaf('q',20889),List('z','k','w','y','h','q'),41497),List('x','j','f','z','k','w','y','h','q'),72127),List('d','x','j','f','z','k','w','y','h','q'),128396),List('s','d','x','j','f','z','k','w','y','h','q'),250291),Fork(Fork(Leaf('o',82762),Leaf('l',83668),List('o','l'),166430),Fork(Fork(Leaf('m',45521),Leaf('p',46335),List('m','p'),91856),Leaf('u',96785),List('m','p','u'),188641),List('o','l','m','p','u'),355071),List('s','d','x','j','f','z','k','w','y','h','q','o','l','m','p','u'),605362),Fork(Fork(Fork(Leaf('r',100500),Fork(Leaf('c',50003),Fork(Leaf('v',24975),Fork(Leaf('g',13288),Leaf('b',13822),List('g','b'),27110),List('v','g','b'),52085),List('c','v','g','b'),102088),List('r','c','v','g','b'),202588),Fork(Leaf('n',108812),Leaf('t',111103),List('n','t'),219915),List('r','c','v','g','b','n','t'),422503),Fork(Leaf('e',225947),Fork(Leaf('i',115465),Leaf('a',117110),List('i','a'),232575),List('e','i','a'),458522),List('r','c','v','g','b','n','t','e','i','a'),881025),List('s','d','x','j','f','z','k','w','y','h','q','o','l','m','p','u','r','c','v','g','b','n','t','e','i','a'),1486387)

  val secret: List[Bit] = List(0,0,1,1,1,0,1,0,1,1,1,0,0,1,1,0,1,0,0,1,1,0,1,0,1,1,0,0,1,1,1,1,1,0,1,0,1,1,0,0,0,0,1,0,1,1,1,0,0,1,0,0,1,0,0,0,1,0,0,0,1,0,1)

  def decodedSecret: List[Char] = decode(frenchCode, secret)




  def encode(tree: CodeTree)(text: List[Char]): List[Bit] = {
    def lookup(tree:  CodeTree)(c: Char): List[Bit] = tree match {
      case Leaf(_, _) => List()
      case Fork(left, right, _, _) if chars(left).contains(c) => 0 :: lookup(left)(c)
      case Fork(left, right, _, _) => 1 :: lookup(right)(c)
    }

    text flatMap lookup(tree)
  }



  type Code = (Char, List[Bit])
  type CodeTable = List[Code]

  def codeBits(table: CodeTable)(char: Char): List[Bit] = {
    table.filter( (code) => code._1 == char ).head._2
  }

  def convert(tree: CodeTree): CodeTable = tree match {
    case Leaf(c, w) => List( (c, List()) )
    case Fork(left, right, cs, w) => mergeCodeTables(convert(left), convert(right))
  }

  def mergeCodeTables(a: CodeTable, b: CodeTable): CodeTable = {
    def prepend(b: Bit)(code: Code): Code = (code._1, b :: code._2)

    a.map(prepend(0)) ::: b.map(prepend(1))
  }

  def quickEncode(tree: CodeTree)(text: List[Char]): List[Bit] = text flatMap codeBits(convert(tree))
}

object Main extends App {
  import Huffman._

  val t1 = Fork(Fork(Leaf('a',2), Leaf('b',3), List('a','b'), 5), Leaf('d',4), List('a','b','d'), 9)
  val enc1 = encode(t1)(string2Chars("abd"))
  println(enc1 )
  println(quickEncode(t1)(string2Chars("abd")))

  println(decodedSecret)
}