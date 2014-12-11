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

import org.w3c.dom.{Node => XMLNode};

/**
 * Описывает условия проверки XML узла, как например что узел является тегом определенного имени и т.д.
 */
abstract class XTest
{
	/**
	 * Проверка узла
	 * @param node Проверяемый узел
	 * @return Условие истинно
	 */
	def test( node: XMLNode ):Boolean = this match {
		case And( e1, e2 ) => e1.test(node) && e2.test(node);
		case Or( e1, e2 ) => e1.test(node) || e2.test(node);
		case Not( e ) => !e.test(node);

		case Null() => node==null;

		case TagName( name ) => if( node!=null ) node.getNodeName.equals(name) else false;
		case Attribute(name,value,funCmp, combine, combineParam) => {
			try
			{
				val attribs = node.getAttributes();
				var res = {
					if( attribs!=null ){
						val a = attribs.getNamedItem(name);
						if( a!=null ){
							if( value==null ) { true } else {
								val v = a.getTextContent;
								funCmp(v,value)
							}
						}else false
					}else false
				}
				if( combine!=null && combineParam!=null ){
					res = combine(combineParam,new XTest{override def test(node: XMLNode):Boolean = res;}).test(node);
				}
				res
			}catch {
				case e: java.lang.NullPointerException => {
					println("fuck");
					false;
				}
			}
		}

		case Sibling( offset, e, nulls ) =>
			var current = node;
			val moveNext = offset > 0;
			var step = if( moveNext ) offset else -offset;
			var stop = false;
			while( !stop ){
				if( step<1 ){
					stop = true;
				}else{
					current = if( moveNext ) current.getNextSibling else current.getPreviousSibling;
					if( current==null )stop = true;
					step -= 1;
				}
			}
			e.test( current );
//			if( current!=null )
//				e.test( current );
//			else
//				nulls;

		case Parent( e,nulls ) => if( node.getParentNode==null ) nulls else e.test(node.getParentNode);

		case Ancestor( e ) =>
			var current = node;
			var stop = false;
			var res = false;
			while( !stop ){
				current = current.getParentNode;
				if( current==null ){
					stop = true
				}else{
					if( e.test(current) ){
						res = true
						stop = true;
					}
				}
			}
			res

		case Child( e ) =>
			var stop = false;
			var res = false;
			if( node.hasChildNodes ){
				for( childIdx <- 0 to node.getChildNodes.getLength ){
					if( !stop ){
						val c = node.getChildNodes.item(childIdx);
						if( e.test(c) ){
							res = true;
							stop = true;
						}
					}
				}
			}
			res
			
		case Leaf() => !node.hasChildNodes();
		
		case TrueXTest() => true;
		case FalseXTest() => false;

		// По умолчанию
		case _ => false
	}

	/**
	 * Создет условие из текущего И указаного
	 * @param e1 Добовляемое условие
	 * @return Условие (текуще) И (указанное)
	 */
	def & ( e1:XTest ):XTest = And(this,e1);
	
	/**
	 * Создет условие из текущего ИЛИ указаного
	 * @param e Добовляемое условие
	 * @return Условие (текуще) ИЛИ (указанное)
	 */
	def | ( e:XTest):XTest = Or(this,e);
	def >> (e:XTest):XTest = And(Ancestor(this),e);
	def > (e:XTest):XTest = And(Parent(this),e);
	def < (e:XTest):XTest = And(this,Child(e));
	def unary_! : XTest = Not(this);

	def + ( e:XTest ):XTest = And(this,Sibling(1,e));
	def - ( e:XTest ):XTest = And(Sibling(-1,this),e);

	def +@ ( attributeName:String ):Attribute = Attribute(attributeName,null,Attribute.equals,And,this);
}

case class TrueXTest() extends XTest;
case class FalseXTest() extends XTest;

case class And( e1:XTest, e2:XTest ) extends XTest;
case class Or( e1:XTest, e2:XTest ) extends XTest;
case class Not( e:XTest ) extends XTest;

case class TagName( name:String ) extends XTest;

object Attribute
{
	val digitPattern = java.util.regex.Pattern.compile("(?is)^((\\+|-)?\\d+(\\.\\d+)?)$");
	val digitPatternGroup = 1;

	def extractNumbers( hasValue:String, needValue:String ) : (Double,Double,Boolean) = {
		if( hasValue==null )return (0,0,false);
		if( needValue==null )return (0,0,false);

		val hasM = digitPattern.matcher(hasValue);
		val needM = digitPattern.matcher(needValue);

		if( !hasM.matches )return (0,0,false);
		if( !needM.matches )return (0,0,false);

		val hasN = hasM.group(digitPatternGroup).toDouble;
		val needN = hasM.group(digitPatternGroup).toDouble;

		(hasN,needN,true)
	}

	def equals( hasValue:String, needValue:String ):Boolean = hasValue.equals(needValue);
	def notEquals( hasValue:String, needValue:String ):Boolean = !hasValue.equals(needValue);

	def less( hasValue:String, needValue:String ):Boolean = {
		val ( hasN, needN, valid ) = extractNumbers( hasValue,needValue );
		valid && hasN < needN
	}
	def more( hasValue:String, needValue:String ):Boolean = {
		val ( hasN, needN, valid ) = extractNumbers( hasValue,needValue );
		valid && hasN > needN
	}
	def lessOrEquals( hasValue:String, needValue:String ):Boolean = {
		val ( hasN, needN, valid ) = extractNumbers( hasValue,needValue );
		valid && hasN <= needN
	}
	def moreOrEquals( hasValue:String, needValue:String ):Boolean = {
		val ( hasN, needN, valid ) = extractNumbers( hasValue,needValue );
		valid && hasN >= needN
	}
}
case class Attribute(
		val name:String,
		val value:String=null,
		val boolOp:(String,String)=>Boolean = Attribute.equals
		,val combine : (XTest,XTest) => XTest = null
		,val combineParam : XTest = null
		) extends XTest
{
	def == (value:String):Attribute = new Attribute(name,value,boolOp,combine,combineParam);
	def != (value:String):Attribute = new Attribute(name,value,Attribute.notEquals,combine,combineParam);
	def > (value:String):Attribute = new Attribute(name,value,Attribute.more,combine,combineParam);
	def >= (value:String):Attribute = new Attribute(name,value,Attribute.moreOrEquals,combine,combineParam);
	def <= (value:String):Attribute = new Attribute(name,value,Attribute.lessOrEquals,combine,combineParam);
	def < (value:String):Attribute = new Attribute(name,value,Attribute.less,combine,combineParam);
}

case class Sibling( offset:Int, e:XTest, nullSibling:Boolean = false ) extends XTest;
case class Parent( e: XTest, nullParent:Boolean = false ) extends XTest;
case class Ancestor( e: XTest ) extends XTest;
case class Child( e:XTest ) extends XTest;
case class Leaf() extends XTest;
case class Null() extends XTest;
