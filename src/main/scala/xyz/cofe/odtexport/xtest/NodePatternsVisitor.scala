package xyz.cofe.odtexport.xtest

/**
 * Посетитель XML узлов делегиующий вызовы шаблонам 
 * @param patterns Шаблоны узлов
 */
class NodePatterns extends XMLPathVisitor with NodePatternsVisitor
{
	private var _patterns = List[NodePattern]();
	def patterns = _patterns;

	def this(patterns:NodePattern*) = {
		this();
		for( p <- patterns )this._patterns = p :: this.patterns;
		this._patterns = this._patterns.reverse;
	}

	def this(patterns:Iterable[NodePattern]) = {
		this();
		for( p <- patterns )this._patterns = p :: this._patterns;
		this._patterns = this.patterns.reverse;
	}
}

/**
 * Примесь к посетителю XMLPathVisitor, делегирующего вызовы к шаблонам узлов
 */
trait NodePatternsVisitor extends XMLPathVisitor {
	import org.w3c.dom.{Node => XMLNode};
	
	/**
	 * Используемые шаблоны
	 * @return XML шаблоны
	 */
	def patterns : List[NodePattern];

	/**
	 * Вызывается при входе в узел XML.
	 * Делегиует вызовы к шаблонам.
	 * Передается управление тому шаблону кто первый ответил на NodePattern.test(node) утвердительно. 
	 * @param node Узел XML
	 * @return Результат делегирования (если было совпадение с шаблоном), либо true по умолчанию.
	 */
	override def enter(node:XMLNode):Boolean = {
		super.enter(node);

		var res = true;
		var stop = false;
		for( pattern <- patterns ){
			if( !stop & pattern.test(node) ){
				res = pattern.enter(node);
				stop = true;
			}
		}
		res;
	}

	/**
	 * Вызывается при выходе из узла
	 * Делегиует вызовы к шаблонам.
	 * Передается управление тому шаблону кто первый ответил на NodePattern.test(node) утвердительно. 
	 * @param node Узел XML
	 */
	override def exit(node:XMLNode):Unit = {
		var stop = false;
		for( pattern <- patterns ){
			if( !stop & pattern.test(node) ){
				pattern.exit(node);
				stop = true;
			}
		}
		
		super.exit(node);
	}
	
	/**
	 * Вызывается перед началом обработки дерева.
	 * Делегирует вызовы к шаблонам.
	 */
	override def begin():Unit = {
		for( ptr <- patterns )
			ptr.begin();
	}

	/**
	 * Вызывается после обработки дерева.
	 * Делегирует вызовы к шаблонам.
	 */
	override def end():Unit = {
		for( ptr <- patterns.reverse )
			ptr.end();
	}
}