import com.sun.jna.Structure
import com.sun.jna.Union
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.platform.win32.WTypes
import com.sun.jna.platform.win32.WinDef

@Structure.FieldOrder("vt", "wReserved1", "wReserved2", "wReserved3", "value")
class PROPVARIANT : Structure() {
    @JvmField
    var vt: Short = 0
    @JvmField
    var wReserved1: Short = 0
    @JvmField
    var wReserved2: Short = 0
    @JvmField
    var wReserved3: Short = 0
    @JvmField
    var value: Value = Value()

    class Value : Union() {
        @JvmField
        var boolVal: WinDef.BOOL? = null
        @JvmField
        var pwszVal: WTypes.LPWSTR? = null
        // Add other types as needed
    }
}
@Structure.FieldOrder("fmtid", "pid")
class PROPERTYKEY(guid: GUID, piid: Int) : Structure() {
    @JvmField
    var fmtid: GUID = guid
    @JvmField
    var pid: Int = piid
}