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

package xyz.cofe.odtexport.test

object testOpPrior
{
	var co = 0;

	class O(var txt:String)
	{
		def | (o:O):O = {
			val r = new O(this+"|"+o)
			co += 1;
			println(co + "."+r);
			r
		}
		def & (o:O):O = {
			val r = new O(this+"&"+o)
			co += 1;
			println(co + "."+r);
			r
		}
		def >> (o:O):O = {
			val r = new O(this+">>"+o)
			co += 1;
			println(co + "."+r);
			r
		}
		def > (o:O):O = {
			val r = new O(this+">"+o)
			co += 1;
			println(co + "."+r);
			r
		}
		def < (o:O):O = {
			val r = new O(this+"<"+o)
			co += 1;
			println(co + "."+r);
			r
		}
		def unary_! : O = {
			val r = new O("!"+this)
			co += 1;
			println(co + "."+r);
			r
		}

		def apply(x:String):O = {
			val r = new O("apply("+x+")")
			co += 1;
			println(co + "."+r);
			r
		}

		override def toString : String = if(txt!=null)txt else "null";
	}

	def main(args:Array[String]):Unit = {
		var o = new O("src");

		println("test 1:");
		o = o("a") >> o("b") | o("c") >> !o("d") & o("e");
		println(o);
	}
}