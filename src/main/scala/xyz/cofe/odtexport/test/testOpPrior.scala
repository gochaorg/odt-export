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