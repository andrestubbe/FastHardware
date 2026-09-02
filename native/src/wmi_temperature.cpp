#include "fasthardware_jni.h"
#include <windows.h>
#include <comdef.h>
#include <WbemIdl.h>

#pragma comment(lib, "wbemuuid.lib")

static bool wmiInitialized = false;
static IWbemServices *pSvc = NULL;      // ROOT\CIMV2
static IWbemServices *pSvcWmi = NULL;   // ROOT\WMI  (for ACPI thermal zones)
static IWbemLocator *pLoc = NULL;

void initWMI() {
    if (wmiInitialized) return;
    
    HRESULT hres = CoInitializeEx(0, COINIT_MULTITHREADED);
    if (FAILED(hres) && hres != RPC_E_CHANGED_MODE) return;
    
    hres = CoInitializeSecurity(
        NULL, -1, NULL, NULL, RPC_C_AUTHN_LEVEL_DEFAULT,
        RPC_C_IMP_LEVEL_IMPERSONATE, NULL, EOAC_NONE, NULL
    );
    
    hres = CoCreateInstance(
        CLSID_WbemLocator, 0, CLSCTX_INPROC_SERVER,
        IID_IWbemLocator, (LPVOID *)&pLoc
    );
    if (FAILED(hres)) return;
    
    // Connect ROOT\CIMV2 (RAM, GPU info, etc.)
    hres = pLoc->ConnectServer(
        _bstr_t(L"ROOT\\CIMV2"), NULL, NULL, 0, NULL, 0, 0, &pSvc
    );
    if (SUCCEEDED(hres)) {
        CoSetProxyBlanket(pSvc, RPC_C_AUTHN_WINNT, RPC_C_AUTHZ_NONE, NULL,
            RPC_C_AUTHN_LEVEL_CALL, RPC_C_IMP_LEVEL_IMPERSONATE, NULL, EOAC_NONE);
    }
    
    // Connect ROOT\WMI separately (ACPI thermal zones live here)
    hres = pLoc->ConnectServer(
        _bstr_t(L"ROOT\\WMI"), NULL, NULL, 0, NULL, 0, 0, &pSvcWmi
    );
    if (SUCCEEDED(hres)) {
        CoSetProxyBlanket(pSvcWmi, RPC_C_AUTHN_WINNT, RPC_C_AUTHZ_NONE, NULL,
            RPC_C_AUTHN_LEVEL_CALL, RPC_C_IMP_LEVEL_IMPERSONATE, NULL, EOAC_NONE);
    }
    
    wmiInitialized = true;
}

// Generic WMI query on a given service connection
double queryWmiDouble(IWbemServices* svc, const wchar_t* query, const wchar_t* propertyName, double fallback) {
    if (!svc) return fallback;
    
    IEnumWbemClassObject* pEnumerator = NULL;
    HRESULT hres = svc->ExecQuery(
        bstr_t("WQL"), bstr_t(query),
        WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY,
        NULL, &pEnumerator
    );
    if (FAILED(hres) || !pEnumerator) return fallback;
    
    IWbemClassObject *pclsObj = NULL;
    ULONG uReturn = 0;
    double result = fallback;
    bool found = false;
    
    while (pEnumerator->Next(WBEM_INFINITE, 1, &pclsObj, &uReturn) == WBEM_S_NO_ERROR) {
        if (uReturn == 0) break;
        VARIANT vtProp;
        VariantInit(&vtProp);
        HRESULT hr = pclsObj->Get(propertyName, 0, &vtProp, 0, 0);
        if (SUCCEEDED(hr)) {
            if (vtProp.vt == VT_I4)  { result = (double)vtProp.lVal;  found = true; }
            if (vtProp.vt == VT_UI4) { result = (double)vtProp.ulVal; found = true; }
            if (vtProp.vt == VT_R8)  { result = vtProp.dblVal;        found = true; }
        }
        VariantClear(&vtProp);
        pclsObj->Release();
        if (found) break;
    }
    pEnumerator->Release();
    return result;
}

JNIEXPORT jdouble JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetCpuTemperatureCelsius
  (JNIEnv *env, jclass clazz) {
    if (!wmiInitialized) initWMI();

    // PRIMARY: MSAcpi_ThermalZoneTemperature in ROOT\WMI (tenths of Kelvin)
    double raw = queryWmiDouble(pSvcWmi,
        L"SELECT CurrentTemperature FROM MSAcpi_ThermalZoneTemperature",
        L"CurrentTemperature", -1.0);
    if (raw > 0) {
        return (raw / 10.0) - 273.15;
    }

    // FALLBACK: Win32_PerfFormattedData_Counters_ThermalZoneInformation (millidegrees C)
    double perf = queryWmiDouble(pSvc,
        L"SELECT Temperature FROM Win32_PerfFormattedData_Counters_ThermalZoneInformation",
        L"Temperature", -1.0);
    if (perf > 0) {
        return perf / 10.0; // millidegrees -> degrees
    }

    return 0.0; // genuinely not available
}

JNIEXPORT jdouble JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetGpuTemperatureCelsius
  (JNIEnv *env, jclass clazz) {
    if (!wmiInitialized) initWMI();

    // Win32_VideoController has no temp on most systems.
    // Try OpenHardwareMonitor WMI namespace if available (community tool).
    double ohm = queryWmiDouble(pSvc,
        L"SELECT Value FROM Sensor WHERE SensorType='Temperature' AND Parent LIKE '%GPU%'",
        L"Value", -1.0);
    if (ohm > 0) return ohm;

    return 0.0; // Intel Xe integrated: no exposed GPU thermal zone
}
