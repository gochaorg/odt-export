/* 
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного 
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"), 
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на 
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование 
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется 
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии 
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ, 
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ, 
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ 
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ 
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ 
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ 
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

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