package xyz.cofe.odtexport

import java.util.Date
import org.w3c.dom._;
import javax.xml.xpath._;
import scala.collection._;

/**
 * Класс расширение org.w3c.dom.Node
 */
class XMLNode ( var xmlNode : Node )
{
	// Ну очень упрощенное форматирование
	private def xmlOf( o : AnyRef, level : Int ) : String = {
		if( o.isInstanceOf[Element] )return xmlOf( o.asInstanceOf[Element],level );
		if( o.isInstanceOf[Document] )return xmlOf( o.asInstanceOf[Document],level );
		if( o.isInstanceOf[Text] )return xmlOf( o.asInstanceOf[Text],level );
		if( o.isInstanceOf[Comment] )return xmlOf( o.asInstanceOf[Comment],level );
		if( o!=null )return o.toString();
		return "";
	}

	private def xmlOf( e : Comment, level : Int ) : String = {
		val txt = "<!--" + e.getData + "-->";
		return txt.toString;
	}

	private def xmlOf( e : Text, level : Int ) : String = {
		val de = e.getData;
		return de.toString;
	}

	private def xmlOf( e : Document, level : Int ) : String = {
		val de = e.getDocumentElement;
		if( de!=null ) return xmlOf( de,level );
		return e.toString;
	}

	private def xmlOf( e : Element, level : Int ) : String = {
		var spacer = "";
		if( level>0 )for( i <- 0 to level-1 )spacer = "  " + spacer;

		var res = spacer + "<"+e.getNodeName();

		if( e.hasAttributes ){
			val nnm = e.getAttributes;
			val co = nnm.getLength;
			for( i <- 0 to (co-1) ){
				res += " ";
				val node = nnm.item(i);
				val name = node.getNodeName;
				val nval = node.getNodeValue;
				res += (
					(if( name!=null ) name else "") +
					(if( nval!=null ) "=\""+nval+"\"" else "")
					);
			}
		}

		res += (if( e.hasChildNodes ) ">\n" else "/>\n");

		if( e.hasChildNodes ){
			val cn = e.getChildNodes;
			val co = cn.getLength;
			for( i <- 0 to co-1 ){
				res += xmlOf( cn.item(i),level+1 );
			}
			res += "\n" + spacer + "</"+e.getNodeName+">\n";
		}

		return res;
	}

	def toXMLString() : String = {
		return xmlOf( xmlNode,0 );
	};

	def xpathNodes(expr:String):NodeList = {
		val xpath = XMLNode.xpathExpression(expr);
		if( xpath==null )return null;

		val res = xpath.evaluate(xmlNode, XPathConstants.NODESET);
		if( res!=null && res.isInstanceOf[NodeList] )return res.asInstanceOf[NodeList];
		null
	}

	def xpathString(expr:String):String = {
		val xpath = XMLNode.xpathExpression(expr);
		if( xpath==null )return null;

		val res = xpath.evaluate(xmlNode, XPathConstants.STRING);
		if( res!=null && res.isInstanceOf[String] )return res.asInstanceOf[String];
		null
	}

	def xpathNumber(expr:String):Double = {
		val xpath = XMLNode.xpathExpression(expr);
		if( xpath==null )return 0;

		val res = xpath.evaluate(xmlNode, XPathConstants.NUMBER);
		if( res!=null && res.isInstanceOf[Double] )return res.asInstanceOf[Double];
		0
	}

	def xpathBoolean(expr:String):Boolean = {
		val xpath = XMLNode.xpathExpression(expr);
		if( xpath==null )return false;

		val res = xpath.evaluate(xmlNode, XPathConstants.BOOLEAN);
		if( res!=null && res.isInstanceOf[Boolean] )return res.asInstanceOf[Boolean];
		false
	}

	def xpathNode(expr:String):Node = {
		val xpath = XMLNode.xpathExpression(expr);
		if( xpath==null )return null;

		val res = xpath.evaluate(xmlNode, XPathConstants.NODE);
		if( res!=null && res.isInstanceOf[Node] )return res.asInstanceOf[Node];
		null
	}
}

class XMLNodes (val nl : NodeList) extends Iterable[Node]
{
	class itr extends Iterator[Node]
	{
		private var i = 0;
		private val co = nl.getLength;
		override def hasNext = i<co;
		override def next = {i+=1;nl.item(i-1)}
	}
	override def iterator:Iterator[Node] = new itr();

	def apply(idx:Int):Node = nl.item(idx); 
}

object XMLNode
{
	import scala.collection.mutable.HashMap;

	private lazy val xpathFactory = XPathFactory.newInstance;
	private lazy val xpath = xpathFactory.newXPath;

	private val cacheExpr : HashMap[String,XPathExpression] = new HashMap();
	private val lastCall : HashMap[String,Date] = new HashMap();
	private val cacheLifeTime : Long = 1000*60; //in milisec
	private var lastClear : Date = null; //date last clear

	def xpathExpression(expr:String):XPathExpression = {
		if( expr==null )throw new IllegalArgumentException( "expr==null" );
		var result : XPathExpression = null;

		// Проверяем в кеше скопилированное выражение
		if( cacheExpr.contains(expr) ){
			//Берем из кеша
			lastCall += expr -> new Date();
			result = cacheExpr(expr);
		}else{
			//Копилим
			val e = xpath.compile(expr);
			cacheExpr += expr -> e;
			lastCall += expr -> new Date();
			result = e;
		}

		val now = new Date();
		if( lastClear==null ||
				((now.getTime()) - (lastClear.getTime())) > cacheLifeTime
		){
			// Необходимо почистить кеш
			var keysToRemove =
				lastCall.filter( kv => (now.getTime() - kv._2.getTime()) > cacheLifeTime ).keys;

			cacheExpr --= keysToRemove;
			lastCall --= keysToRemove;
		}

		result
	}

	/**
	 * Функция преобразования org.w3c.dom.Node =&gt; XMLNode
	 */
	implicit def node2ext( node : Node ) : XMLNode = new XMLNode( node );

	/**
	 * Функция преобразования org.w3c.dom.NodeLisr =&gt; XMLNodes
	 */
	implicit def nodes2ext( nl : NodeList ) : XMLNodes = new XMLNodes( nl );
}
