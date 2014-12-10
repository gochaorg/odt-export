package xyz.cofe.odtexport

import java.sql.Connection
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

object Sql {
	/**
	 * Список зарегистрированных драйверов
	 */
	private var registeredDrivers : List[String] = Nil;

	/**
	 * Регистрация драйвера JDBC
	 * @param driverClass Класс JDBC драйвера
	 */
	def registerDriver( driverClass:String ) = {
		if( driverClass!=null ){
			if(!registeredDrivers.contains(driverClass)){
				java.lang.Class.forName(driverClass).newInstance;
				registeredDrivers = driverClass :: registeredDrivers;
			}
		}
	}

	/**
	 * Соединение с СУБД
	 * @param driverClass Класс драйвера JDBC
	 * @param jdbcUrl JDBC Строка соединения
	 * @return Соединеие
	 */
	def connect( driverClass:String, jdbcUrl:String ):Connection = {
		registerDriver( driverClass );
		DriverManager.getConnection(jdbcUrl);
	}

	/**
	 * Соединение с СУБД
	 * @param driverClass Класс драйвера JDBC
	 * @param jdbcUrl JDBC Строка соединения
	 * @param login Логин пользователя СУБД
	 * @param password Пароль пользователя СУБД
	 * @return Соединеие
	 */
	def connect( driverClass:String, jdbcUrl:String, login:String, password:String ):Connection = {
		registerDriver( driverClass );
		DriverManager.getConnection(jdbcUrl,login,password);
	}

	/**
	 * Соединение с СУБД
	 * @param driverClass Класс драйвера JDBC
	 * @param jdbcUrl JDBC Строка соединения
	 * @param props Свойсва соединения
	 * @return Соединеие
	 */
	def connect( driverClass:String, jdbcUrl:String, props:Properties):Connection = {
		registerDriver( driverClass );
		DriverManager.getConnection(jdbcUrl,props);
	}

	/**
	 * Соединение с СУБД
	 * @param driverClass Класс драйвера JDBC
	 * @param jdbcUrl JDBC Строка соединения
	 * @return Соединеие
	 */
	def connect(jdbcUrl:String ):Connection = {
		DriverManager.getConnection(jdbcUrl);
	}

	/**
	 * Соединение с СУБД
	 * @param jdbcUrl JDBC Строка соединения
	 * @param login Логин пользователя СУБД
	 * @param password Пароль пользователя СУБД
	 * @return Соединеие
	 */
	def connect( jdbcUrl:String, login:String, password:String ):Connection = {
		DriverManager.getConnection(jdbcUrl,login,password);
	}

	/**
	 * Соединение с СУБД
	 * @param jdbcUrl JDBC Строка соединения
	 * @param props Свойсва соединения
	 * @return Соединеие
	 */
	def connect( jdbcUrl:String, props:Properties):Connection = {
		DriverManager.getConnection(jdbcUrl,props);
	}

	/**
	 * Расширение соединения
	 */
	implicit def extendConnection( connection:Connection ):ConnectionExtension = new ConnectionExtension(connection);

	class ConnectionExtension (val connection:Connection)
	{
		def executeQuery(query:String) : (ResultSet,Statement) = {
			val s = connection.createStatement;
			val rs = s.executeQuery(query);
			(rs,s)
		}

		def executeQuery(query:String,params:Any*) : (ResultSet,Statement) = {
			if( params.length>0 ){
				val s = connection.prepareStatement(query);
				for( i <- 0 to params.length-1 ){
					s.setObject(i+1,params(i));
				}
				val rs = s.executeQuery;
				return (rs,s);
			}
			val s = connection.createStatement;
			val rs = s.executeQuery(query);
			(rs,s)
		}

		def insert(query:String):(Int,ResultSet,Statement) = {
			val s = connection.createStatement;
			val insertCount = s.executeUpdate(query,java.sql.Statement.RETURN_GENERATED_KEYS);
			(insertCount,s.getGeneratedKeys,s)
		}

		def insert(query:String,params:Any*):(Int,ResultSet,Statement) = {
			if( params.length>0 ){
				val s = connection.prepareStatement(query,java.sql.Statement.RETURN_GENERATED_KEYS);
				for( i <- 0 to params.length-1 ){
					s.setObject(i+1,params(i));
				}
				val insertCount = s.executeUpdate();
				(insertCount,s.getGeneratedKeys,s)
			}
			val s = connection.createStatement;
			val insertCount = s.executeUpdate(query,java.sql.Statement.RETURN_GENERATED_KEYS);
			(insertCount,s.getGeneratedKeys,s)
		}

		def update(query:String):Int = {
			val s = connection.createStatement;
			val updateCount = s.executeUpdate(query);
			s.close;
			updateCount
		}

		def update(query:String,params:Any*):Int = {
			if( params.length>0 ){
				val s = connection.prepareStatement(query);
				for( i <- 0 to params.length-1 ){
					s.setObject(i+1,params(i));
				}
				val updateCount = s.executeUpdate();
				s.close;
				return updateCount;
			}
			val s = connection.createStatement;
			val updateCount = s.executeUpdate(query);
			s.close;
			updateCount
		}

		def delete(query:String):Int = update(query);
		def delete(query:String,params:Any*):Int = update(query,params);
	}

	/**
	 * Представляет механизм доступа к строке выборки.
	 */
	class Row (val rs:ResultSet)
	{
		/**
		 * Метка колонки без разници к регистру
		 */
		def apply(label:String):AnyRef = rs.getObject(label);

		/**
		 * Индекс колонки от нуля (from 0)
		 */
		def apply(idx:Int):AnyRef = rs.getObject(idx+1);
	}

	/**
	 *  Расширение ResultSet
	 */
	implicit def extendResultSet( rs:ResultSet ):ResultSetExtension = new ResultSetExtension(rs);

	/**
	 * Расширение интерфейса java.sql.ResultSet
	 */
	class ResultSetExtension( val rs:ResultSet ){
		object columns
		{
			def length : Int = rs.getMetaData.getColumnCount

			def apply(columnIndex:Int) = new {
				def label() = rs.getMetaData.getColumnLabel(columnIndex+1);
				def name() = rs.getMetaData.getColumnName(columnIndex+1);
				def sqlType() = rs.getMetaData.getColumnType(columnIndex+1);
				def sqlTypeName() = rs.getMetaData.getColumnTypeName(columnIndex+1);
				def precision()  = rs.getMetaData.getPrecision(columnIndex+1);
				def scale()  = rs.getMetaData.getScale(columnIndex+1);
				def schemaName()  = rs.getMetaData.getSchemaName(columnIndex+1);
				def tableName()  = rs.getMetaData.getTableName(columnIndex+1);
				def className()  = rs.getMetaData.getColumnClassName(columnIndex+1);
				def catalogName()  = rs.getMetaData.getCatalogName(columnIndex+1);
				def displaySize()  = rs.getMetaData.getColumnDisplaySize(columnIndex+1);
				def isAutoIncrement() = rs.getMetaData.isAutoIncrement(columnIndex+1);
				def isNullable() = rs.getMetaData.isNullable(columnIndex+1);
				def isCurrency() = rs.getMetaData.isCurrency(columnIndex+1);
				def isCaseSensitive() = rs.getMetaData.isCaseSensitive(columnIndex+1);
				def isDefinitelyWritable() = rs.getMetaData.isDefinitelyWritable(columnIndex+1);
				def isReadOnly() = rs.getMetaData.isReadOnly(columnIndex+1);
				def isSearchable() = rs.getMetaData.isSearchable(columnIndex+1);
				def isSigned() = rs.getMetaData.isSigned(columnIndex+1);
				def isWritable() = rs.getMetaData.isWritable(columnIndex+1);
			}
		}

		def readAsArray( reciver:Array[AnyRef] => Unit ):Unit = {
			val colCount = columns.length;
			while(rs.next){
				var arr = new Array[AnyRef](colCount);
				for( i <- 0 to colCount-1 ){
					arr(i) = rs.getObject(i+1);
				}
				reciver(arr);
			}
		}

		def readAsMap( reciver:Map[String,AnyRef] => Unit ):Unit = {
			val colCount = columns.length;
			var colNames : List[String] = Nil;
			for( c <- 0 to colCount-1 ){
				colNames = columns(c).label :: colNames;
			}
			colNames = colNames.reverse;
			while(rs.next){
				var arr = Map[String,AnyRef]();
				for( i <- 0 to colCount-1 ){
					arr = arr + (colNames(i) -> rs.getObject(i+1));
				}
				reciver(arr);
			}
		}

		def read( reciver:Row => Unit ):Unit = {
			if( reciver==null )return;
			while(rs.next){
				reciver( new Row(rs) );
			}
		}
	}
}