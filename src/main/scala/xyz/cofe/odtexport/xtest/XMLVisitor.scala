package xyz.cofe.odtexport.xtest

import org.w3c.dom.{Node => XMLNode};
import scala.collection.immutable.Stack;

/**
 * Интефейс обхода дерева
 */
trait XMLVisitor
{
	/**
	 * Вызывается при входе в узел XML
	 * @param node Узел XML
	 * @return True - Входить в дочение элементы
	 */
	def enter(node:XMLNode):Boolean;

	/**
	 * Вызывается при выходе из узла
	 * @param node Узел XML
	 */
	def exit(node:XMLNode):Unit;
}

/**
 * Объект обхода древа
 */
object XMLVisitor
{
	/**
	 * Обходит древо XML
	 * @param node - Корень c которого происходит дерева
	 * @param visitor - Постетитель XML узлов
	 */
	def go( node:XMLNode, visitor:XMLVisitor ):Unit = {
		val enterInner = visitor.enter(node);
		if( node.hasChildNodes && enterInner ){
			for( i <- 0 to node.getChildNodes.getLength - 1 ){
				go( node.getChildNodes.item(i), visitor );
			}
		}
		visitor.exit(node);
	}

	/**
	 * Создает итератор по древу XML
	 * @param node - Корень c которого происходит дерева
	 * @return Итератор
	 */
	def go( node:XMLNode ):Iterable[XMLNode] = {
		new Iterable[XMLNode]{
			def iterator : Iterator[XMLNode] = new Iterator[XMLNode] {
				def next:XMLNode = {null}
				def hasNext:Boolean = false;
			}
		}
	}
}

/**
 * Обходит дерево, сохраняя текуший путь в стеке, и отмечаяя начало и конец обхода
 */
class XMLPathVisitor extends scala.AnyRef with XMLVisitor
{
	/**
	 * Текущий путь
	 */
	var path = new Stack[XMLNode]();

	/**
	 * Вызывается в начале обхода
	 */
	def begin():Unit = {}

	/**
	 * Вызывается в конце обхода
	 */
	def end():Unit = {}

	/**
	 * Вызывается при входе в узел XML
	 * @param node Узел XML
	 * @return True - Входить в дочение элементы
	 */
	def enter(node:XMLNode):Boolean = {
		if( path.length==0 )begin();
		path = path.push( node );
		true
	}
	
	/**
	 * Вызывается при выходе из узла
	 * @param node Узел XML
	 */
	def exit(node:XMLNode):Unit = {
		if( path.length>0 )path = path.pop;
		if( path.length==0 )end();
	}
}