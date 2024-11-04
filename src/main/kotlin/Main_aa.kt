//package org.example
//
//import com.sun.jna.Pointer
//import com.sun.jna.platform.win32.COM.COMUtils
//import com.sun.jna.platform.win32.COM.Unknown
//import com.sun.jna.platform.win32.Guid
//import com.sun.jna.platform.win32.Guid.CLSID
//import com.sun.jna.platform.win32.Ole32
//import com.sun.jna.platform.win32.WTypes
//import com.sun.jna.platform.win32.WinNT.HRESULT
//import com.sun.jna.ptr.PointerByReference
//
//fun main(){
//    println("Hello world!")
//    Ole32.INSTANCE.CoInitializeEx(null, 0) // 0 is multi threaded
//    println("Calling MMDeviceEnumerator.create()")
//    val mmEnumerator = MMDeviceEnumerator.create()
//    println("Got MMDeviceEnumerator")
//}
//
//internal class MMDeviceEnumerator (p: Pointer?) : Unknown(p) {
//    // functionz
//    fun GetDefaultAudioEndpoint(dataFlow: Int, role: Int, ppEndpoint: PointerByReference) {
//        val res = _invokeNativeObject(
//            2,
//            arrayOf(pointer, dataFlow, role, ppEndpoint), HRESULT::class.java
//        ) as HRESULT
//
//        COMUtils.checkRC(res)
//        println("response: $res")
//    }
//
//    companion object {
//        val CLSID_MMDeviceEnumerator: CLSID = CLSID("bcde0395-e52f-467c-8e3d-c4579291692e")
//        val IID_IMMDeviceEnumerator: Guid.GUID = Guid.GUID("a95664d2-9614-4f35-a746-de8db63617e6")
//
//        fun create(): MMDeviceEnumerator? {
//            val pEnumerator = PointerByReference()
//
//            val hres = Ole32.INSTANCE.CoCreateInstance(
//                CLSID_MMDeviceEnumerator, null,
//                WTypes.CLSCTX_ALL, IID_IMMDeviceEnumerator,
//                pEnumerator
//            )
//            println("response: $hres")
//            if (COMUtils.FAILED(hres)) {
//                return null
//            }
//
//            return MMDeviceEnumerator(pEnumerator.value)
//        }
//    }
//}