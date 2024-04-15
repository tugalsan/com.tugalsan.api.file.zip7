module com.tugalsan.api.file.zip7 {
    requires sevenzipjbinding;
    requires org.apache.commons.compress;
    requires com.tugalsan.api.os;
    requires com.tugalsan.api.union;
    requires com.tugalsan.api.bytes;
    exports com.tugalsan.api.file.zip7.server;
}
