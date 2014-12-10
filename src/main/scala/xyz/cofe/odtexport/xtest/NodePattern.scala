package xyz.cofe.odtexport.xtest

/**
 * Шаблон обработки XML узла 
 */
trait NodePattern extends XTest
{
	import org.w3c.dom.{Node => XMLNode};

	/**
	 * Вызывается при входе в узел XML
	 * @param node Узел XML
	 * @return True - Входить в дочение элементы
	 */
	def enter(node:XMLNode):Boolean = true;

	/**
	 * Вызывается при выходе из узла
	 * @param node Узел XML
	 */
	def exit(node:XMLNode):Unit = {};
	
	/**
	 * Вызывается перед началом обработки первого узла
	 */
	def begin():Unit = {};
	
	/**
	 * Вызывается после обработки последнего узла
	 */
	def end():Unit = {};
}

/**
 * Базовая реализация шаблона обработки XML узла
 * @param xtest - Условие совпадения текущего узла древа.
 * @param delegate - Шаблон которому делегируются вызовы (возможно null).
 */
class NodePatternBasic( val xtest:XTest, val delegate:NodePatternBasic = null ) extends XTest with NodePattern
{
	import org.w3c.dom.{Node => XMLNode};
	
	/**
	 * Проверяет совпадение узла с указанным условием
	 * @param node XML узел
	 */
	override def test(node:XMLNode):Boolean = xtest.test(node);
	
	/**
	 * Вызывается при предобработке узла и делегурет вызов (если указан делегат)
	 */
	override def enter(node:XMLNode):Boolean = {
		if( delegate!=null )
			return delegate.enter(node);
		true;
	}
	
	/**
	 * Вызывается при постобработке узла и делегурет вызов (если указан делегат)
	 */
	override def exit(node:XMLNode):Unit = if( delegate!=null )delegate.exit(node);

	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * @param f Функц предобработки узла
	 * @return Шаблон с функцией.
	 */
	def enter( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def enter(node:XMLNode):Boolean = {
			super.enter(node);
			f;
			true
		}
	}
	
	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * При этом вхождение в дочерние не будет происходить.
	 * @param f Функц предобработки узла
	 * @return Шаблон с функцией.
	 */
	def skip( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def enter(node:XMLNode):Boolean = {
			super.enter(node);
			f;
			false
		}
	}
	
	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def enterNode( f: (XMLNode)=>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f(node);
			true
		}
	};
	
	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * При этом вхождение в дочерние не будет происходить.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def skipNode( f: (XMLNode)=>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f(node);
			false
		}
	};
	
	/**
	 * Создает шаблон с функц. пост срабатывающий XML узела.
	 * @param f Функц постобработки узла
	 * @return Шаблон с функцией.
	 */
	def exit( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def exit(node:XMLNode):Unit = {
			f;
			super.exit(node);
		}
	}
	
	/**
	 * Создает шаблон с функц. пост срабатывающий XML узела.
	 * @param f Функц постобработки узла
	 * @return Шаблон с функцией.
	 */
	def exitNode( f: (XMLNode)=>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def exit(node:XMLNode):Unit = {
			f(node);
			super.exit(node);
		}
	}
	
	override def begin():Unit = if( delegate!=null ) delegate.begin();
	override def end():Unit = if( delegate!=null ) delegate.end();
	
	def begin( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def begin():Unit = {
			super.begin();
			f;
		}
	}

	def end( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest,this){
		override def end():Unit = {
			f;
			super.end();
		}
	}
}

/**
 * Объект с неявным преобразователем Условие в Шаблон 
 */
object NodePattern
{
	/**
	 * Преобразователь Условие в Шаблон
	 * @param xtest Условие
	 * @return Пустой шаблон
	 */
	implicit def extendXTest( xtest:XTest ):XTestExtension = new XTestExtension(xtest);
}

/**
 * Расширение условие до возможности создания шаблонов 
 */
class XTestExtension( val xtest:XTest )
{
	import org.w3c.dom.{Node => XMLNode};

	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def enter( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f;
			true
		}
	};

	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * При этом вхождение в дочерние не будет происходить.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def skip( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f;
			false
		}
	};
	
	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def enterNode( f: (XMLNode)=>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f(node);
			true
		}
	};
	
	/**
	 * Создает шаблон с функц. срабатывающий при входе в XML узел.
	 * При этом вхождение в дочерние не будет происходить.
	 * @param f Функц обработки узла
	 * @return Шаблон с функцией.
	 */
	def skipNode( f: (XMLNode)=>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def enter(node:XMLNode):Boolean = {
			f(node);
			false
		}
	};
	
	/**
	 * Создает шаблон с функц. пост срабатывающий XML узела.
	 * @param f Функц постобработки узла
	 * @return Шаблон с функцией.
	 */
	def exit( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest) {
		override def exit(node:XMLNode):Unit = {
			f;
		}
	}

	/**
	 * Создает шаблон с функц. преобработки древа
	 * @param f Функция обработки
	 * @return Шаблон с функцией.
	 */
	def begin( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest){
		override def begin():Unit = {
			super.begin();
			f;
		}
	}

	/**
	 * Создает шаблон с функц. постобработки древа
	 * @param f Функция обработки
	 * @return Шаблон с функцией.
	 */
	def end( f: =>Unit ):NodePatternBasic = new NodePatternBasic(xtest){
		override def end():Unit = {
			f;
			super.end();
		}
	}
}