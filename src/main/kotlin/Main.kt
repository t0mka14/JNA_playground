package org.example

import PROPVARIANT
import PROPERTYKEY
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid.CLSID
import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.PointerByReference


//fun main() {
//    println("Hello World!")
//    val lib = Native.load(if (Platform.isWindows()) "msvcrt" else "c", CMath::class.java)
//    val res = lib.cosh(5.0)
//    println(res)
//
//}


fun main() {
    Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED)

    val PKEY_Device_FriendlyName = PROPERTYKEY(
        GUID("{a45c254e-df1c-4efd-8020-67d146a850e0}"),
        14
    )

    val deviceEnumerator = MMDeviceEnumerator.create()!!
    val deviceCollection = PointerByReference() //v tomhle jsou actual data
    println("Got device enumerator")
    val hr = deviceEnumerator.EnumAudioEndpoints(
        deviceEnumerator.EDataFlow_eAll, deviceEnumerator.DEVICE_STATE_ACTIVE, deviceCollection
    )

    val devices = MMDeviceCollection(deviceCollection.value)
    println("Got device collection with ${devices.GetCount()} devices")
    val count = devices.GetCount()

    for (i in 0 until count) {
        val device = PointerByReference()
        devices.Item(i, device)
        val mmDevice = MMDevice(device.value)
        val id = mmDevice.GetId()
        val state = mmDevice.GetState()
        val propertyStore = mmDevice.OpenPropertyStore(MMDevice.STGM_READ)
        val variant = PROPVARIANT()
        val friendlyName = propertyStore.GetValue(PKEY_Device_FriendlyName, variant)//.value.stringValue
        println("${variant.value.pwszVal?.value}")

        //println("Device $i: $friendlyName (ID: $id)")
        println("Device $i, id: $id (state: $state = ${DeviceStatesEnum.entries.find { it.code == state }?.name})")
    }

    Ole32.INSTANCE.CoUninitialize()
}
interface CMath: Library {
    fun cosh(value: Double) : Double
}




class MMDeviceCollection(p: Pointer?) : Unknown(p) {

    fun GetCount(): Int {
        val count = WinDef.UINTByReference()
        val hr = _invokeNativeObject(
            3,  // `GetCount` is the 3rd method of `IMMDeviceCollectionVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, count),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return count.value.toInt()
    }

    fun Item(index: Int, ppDevice: PointerByReference): HRESULT {
        val hr = _invokeNativeObject(
            4,  // `Item` is the 4th method of `IMMDeviceCollectionVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, WinDef.UINT(index.toLong()), ppDevice),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return hr
    }
}

class IPropertyStore(p: Pointer?) : Unknown(p) {

    fun GetValue(key: PROPERTYKEY, pv: PROPVARIANT): HRESULT {
        val hr = _invokeNativeObject(
            5,  // `GetValue` is the 4th method of `IPropertyStoreVtbl` in `propsys.h`
            arrayOf(pointer, key, pv),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return hr
    }

    fun SetValue(key: PROPERTYKEY, propvar: PROPVARIANT): HRESULT {
        val hr = _invokeNativeObject(
            6,  // `SetValue` is the 5th method of `IPropertyStoreVtbl` in `propsys.h`
            arrayOf(pointer, key, propvar),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return hr
    }

    fun Commit(): HRESULT {
        val hr = _invokeNativeObject(
            7,  // `Commit` is the 6th method of `IPropertyStoreVtbl` in `propsys.h`
            arrayOf(pointer),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return hr
    }
}


class MMDevice(p: Pointer?) : Unknown(p) {

    fun OpenPropertyStore(stgmAccess: Int): IPropertyStore {
        val ppProperties = PointerByReference()
        val hr = _invokeNativeObject(
            4,  // `OpenPropertyStore` is the 4th method of `IMMDeviceVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, stgmAccess, ppProperties),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return IPropertyStore(ppProperties.value)
    }

    fun GetId(): String {
        val ppstrId = PointerByReference()
        val hr = _invokeNativeObject(
            5,  // `GetId` is the 3rd method of `IMMDeviceVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, ppstrId),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return ppstrId.value.getWideString(0)
    }

    fun GetState(): Int {
        val pdwState = IntArray(1)
        val hr = _invokeNativeObject(
            6,  // `GetState` is the 6th method of `IMMDeviceVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, pdwState),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(hr)
        return pdwState[0]
    }

companion object{
    const val STGM_READ = 0
}
}

class MMDeviceEnumerator(p: Pointer) : Unknown(p) {
    val EDataFlow_eAll: Int = 2
    val DEVICE_STATE_ACTIVE: Int = 0x1


//    interface Ole32 : Library {
//        fun CoCreateInstance(
//            rclsid: GUID?,
//            pUnkOuter: Pointer?,
//            dwClsContext: Int,
//            riid: GUID?,
//            ppv: PointerByReference?
//        ): HRESULT?
//
//        companion object {
//            val INSTANCE: Ole32 = Native.load(
//                "Ole32",
//                Ole32::class.java
//            )
//        }
//    }

    fun EnumAudioEndpoints(dataFlow: Int, dwStateMask: Int, ppDevices: PointerByReference): HRESULT {
        val res = _invokeNativeObject(
            3,  // `EnumAudioEndpoints` is the 3rd method of `IMMDeviceEnumeratorVtbl` in `mmdeviceapi.h`
            arrayOf(pointer, dataFlow, DWORD(dwStateMask.toLong()), ppDevices),
            HRESULT::class.java
        ) as HRESULT
        COMUtils.checkRC(res)
        return res
    } // map other functions as needed




    companion object {
        fun create(): MMDeviceEnumerator? {
            val pEnumerator = PointerByReference()

            val hres = Ole32.INSTANCE.CoCreateInstance(
                CLSID_MMDeviceEnumerator, null,
                WTypes.CLSCTX_ALL, IID_IMMDeviceEnumerator, pEnumerator
            )
            println("response: $hres")
            if (COMUtils.FAILED(hres)) {
                println("Failed")
                return null
            }

            return MMDeviceEnumerator(pEnumerator.value)
        }
        val CLSID_MMDeviceEnumerator: CLSID = CLSID("bcde0395-e52f-467c-8e3d-c4579291692e")
        val IID_IMMDeviceEnumerator: GUID = GUID("a95664d2-9614-4f35-a746-de8db63617e6")

        const val EDataFlow_eRender: Int = 0
        const val EDataFlow_eCapture: Int = 1

        const val EDataFlow_enum_count: Int = 3


    }
}
enum class DeviceStatesEnum(val code: Int){
    DEVICE_STATE_ACTIVE(1),
    DEVICE_STATE_DISABLED(2),
    DEVICE_STATE_NOTPRESENT(4),
    DEVICE_STATE_UNPLUGGED(8),
    DEVICE_STATEMASK_ALL(0xF)
}