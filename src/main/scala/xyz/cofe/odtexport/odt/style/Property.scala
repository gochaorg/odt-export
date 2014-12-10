package xyz.cofe.odtexport.odt.style

class Property( val name:String, val value : String = null ) {
    private var _children : List[Property] = List();

    /**
     * Описывает вложенные дочерние свойства
     */
    def children() : List[Property] = _children;

    /**
     * Добавляет дочернее свойство
     */
    def add( child : Property ) : Unit = {
        _children = child :: _children;
    }

    override def toString() : String = {
        val sb = new StringBuilder();
        sb.append(name);
        if( value!=null ){
            sb.append("=\"");
            sb.append(value);
            sb.append("\"");
        }
        if( children.size>0 ){
            sb.append("{\n");
            for( c <- children ){
                sb.append( c.toString );
                sb.append( "\n" );
            }
            sb.append("}")
        }
        sb.toString;
    }
}