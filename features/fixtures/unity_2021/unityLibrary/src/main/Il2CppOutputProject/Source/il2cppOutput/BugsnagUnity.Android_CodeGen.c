#include "pch-c.h"
#ifndef _MSC_VER
# include <alloca.h>
#else
# include <malloc.h>
#endif


#include "codegen/il2cpp-codegen-metadata.h"





// 0x00000001 System.Collections.Generic.Queue`1<T> BugsnagUnity.BlockingQueue`1::get_Queue()
// 0x00000002 System.Object BugsnagUnity.BlockingQueue`1::get_QueueLock()
// 0x00000003 System.Void BugsnagUnity.BlockingQueue`1::.ctor()
// 0x00000004 System.Void BugsnagUnity.BlockingQueue`1::Enqueue(T)
// 0x00000005 T BugsnagUnity.BlockingQueue`1::Dequeue()
// 0x00000006 BugsnagUnity.IClient BugsnagUnity.Bugsnag::Start(BugsnagUnity.IConfiguration)
extern void Bugsnag_Start_m99533D7F5D6993DC8CFE3F3A432292B8017D775D (void);
// 0x00000007 BugsnagUnity.Client BugsnagUnity.Bugsnag::get_InternalClient()
extern void Bugsnag_get_InternalClient_mF728A2D45F03ECC956D01F3762BB26F97567A51B (void);
// 0x00000008 System.Void BugsnagUnity.Bugsnag::set_InternalClient(BugsnagUnity.Client)
extern void Bugsnag_set_InternalClient_m6D24736859831766E8601B676E40F78DA55F70AF (void);
// 0x00000009 BugsnagUnity.IClient BugsnagUnity.Bugsnag::get_Client()
extern void Bugsnag_get_Client_m670578B6828115448054052FBA99F05416BE6293 (void);
// 0x0000000A System.Void BugsnagUnity.Bugsnag::SetApplicationState(System.Boolean)
extern void Bugsnag_SetApplicationState_mE6412B3B45EF1B2FAB1BD1F76AC0AAF9983C0439 (void);
// 0x0000000B System.Void BugsnagUnity.Bugsnag::.cctor()
extern void Bugsnag__cctor_m0127759E15277885D91A47ECA3A691C7A2047FBB (void);
// 0x0000000C BugsnagUnity.IConfiguration BugsnagUnity.Client::get_Configuration()
extern void Client_get_Configuration_mE1CB4A14B8CA8DF7E8BEFDA666559CF26E22832D (void);
// 0x0000000D BugsnagUnity.IBreadcrumbs BugsnagUnity.Client::get_Breadcrumbs()
extern void Client_get_Breadcrumbs_mCB7D0806282EEAE79271AA7CFCBC255B8B1D6B7C (void);
// 0x0000000E BugsnagUnity.ISessionTracker BugsnagUnity.Client::get_SessionTracking()
extern void Client_get_SessionTracking_m065FCB8E6518F43BDE275D0C248F97EAD48AE0DD (void);
// 0x0000000F BugsnagUnity.Payload.User BugsnagUnity.Client::get_User()
extern void Client_get_User_m6353A2AA86F45A292A6191F580E47CC7218DC6D8 (void);
// 0x00000010 BugsnagUnity.Payload.Metadata BugsnagUnity.Client::get_Metadata()
extern void Client_get_Metadata_m472AB7A151D4836CA912F81E9B19F0388A55FEE6 (void);
// 0x00000011 BugsnagUnity.UniqueLogThrottle BugsnagUnity.Client::get_UniqueCounter()
extern void Client_get_UniqueCounter_mB1E77725E2969E024C6C2689F601F80F25C2BD56 (void);
// 0x00000012 BugsnagUnity.MaximumLogTypeCounter BugsnagUnity.Client::get_LogTypeCounter()
extern void Client_get_LogTypeCounter_m25BC7DBF57EA6D2213A338F3F8BA72D553C118FD (void);
// 0x00000013 BugsnagUnity.IDelivery BugsnagUnity.Client::get_Delivery()
extern void Client_get_Delivery_mB8CD39650A2D7A92E747083002BB9374085A687C (void);
// 0x00000014 System.Collections.Generic.List`1<BugsnagUnity.Middleware> BugsnagUnity.Client::get_Middleware()
extern void Client_get_Middleware_mA5BC37B4AC39D6A0479D47F18B43544180069589 (void);
// 0x00000015 System.Object BugsnagUnity.Client::get_MiddlewareLock()
extern void Client_get_MiddlewareLock_m26CB202A5465E8801632755A2877CA6AA449EB19 (void);
// 0x00000016 BugsnagUnity.INativeClient BugsnagUnity.Client::get_NativeClient()
extern void Client_get_NativeClient_mFB509FDC16CF5DC294A7E16CE27498FD065D20DB (void);
// 0x00000017 System.Diagnostics.Stopwatch BugsnagUnity.Client::get_ForegroundStopwatch()
extern void Client_get_ForegroundStopwatch_m6EBF716FC66C5001040DC39E8623B92B9AC91803 (void);
// 0x00000018 System.Diagnostics.Stopwatch BugsnagUnity.Client::get_BackgroundStopwatch()
extern void Client_get_BackgroundStopwatch_mE3930B3A8F63D9F0BB7C7E4E82662A69C3727ECD (void);
// 0x00000019 System.Boolean BugsnagUnity.Client::get_InForeground()
extern void Client_get_InForeground_mDBCC0C882AFF50C6F2F7068CBB806734E3A077CF (void);
// 0x0000001A UnityEngine.GameObject BugsnagUnity.Client::get_TimingTrackerObject()
extern void Client_get_TimingTrackerObject_mA5A4E55F7456B43331337C56881DD0F93815A52C (void);
// 0x0000001B System.Void BugsnagUnity.Client::.ctor(BugsnagUnity.INativeClient)
extern void Client__ctor_m7674A0E55E3F2BB5AC0BDAC207F3C611E4ED5ACC (void);
// 0x0000001C System.Void BugsnagUnity.Client::AddBugsnagLoadedBreadcrumb()
extern void Client_AddBugsnagLoadedBreadcrumb_mDF8135A7E8C39EE9DF85EDEA34EEBC3D42278387 (void);
// 0x0000001D System.Boolean BugsnagUnity.Client::IsUsingFallback()
extern void Client_IsUsingFallback_m3CD71B937AD81497152D85D7DE7734334A936583 (void);
// 0x0000001E System.Void BugsnagUnity.Client::SetupSceneLoadedBreadcrumbTracking()
extern void Client_SetupSceneLoadedBreadcrumbTracking_m5ECF7DE8D7E2A471E3312C9F3480E6BBEE90234A (void);
// 0x0000001F System.Void BugsnagUnity.Client::Send(BugsnagUnity.Payload.IPayload)
extern void Client_Send_m44AE255BF1C699063B6EBBE1E9A170ADDCC04E84 (void);
// 0x00000020 System.Void BugsnagUnity.Client::SceneLoaded(UnityEngine.SceneManagement.Scene,UnityEngine.SceneManagement.LoadSceneMode)
extern void Client_SceneLoaded_m07BEF740BC8D79515AEF0499860E1B72593B19B6 (void);
// 0x00000021 System.Void BugsnagUnity.Client::MultiThreadedNotify(System.String,System.String,UnityEngine.LogType)
extern void Client_MultiThreadedNotify_m239518BF98910651E4B66F2999FBD594FB0518BB (void);
// 0x00000022 System.Void BugsnagUnity.Client::Notify(System.String,System.String,UnityEngine.LogType)
extern void Client_Notify_m327514D399381EEDBD3E7AFAA8E109776634624A (void);
// 0x00000023 System.Void BugsnagUnity.Client::Notify(BugsnagUnity.Payload.Exception[],BugsnagUnity.Payload.HandledState,BugsnagUnity.Middleware,System.Nullable`1<UnityEngine.LogType>)
extern void Client_Notify_mB1085A6AEAAE300592DA78EF4D36E0F1231BDC1E (void);
// 0x00000024 System.Void BugsnagUnity.Client::SetApplicationState(System.Boolean)
extern void Client_SetApplicationState_mDBAD009A37A58D53E23F18800992128204DFD7FF (void);
// 0x00000025 System.Boolean BugsnagUnity.Client::ShouldSendRequests()
extern void Client_ShouldSendRequests_m100A8BC55D6D4BACD474B98A8B9B35E8852F3AEC (void);
// 0x00000026 System.Collections.Generic.IEnumerator`1<UnityEngine.AsyncOperation> BugsnagUnity.Client::RunInitialSessionCheck()
extern void Client_RunInitialSessionCheck_mF16C504BED04D8295FAC6D0B7DC2529C51BA97F3 (void);
// 0x00000027 System.Void BugsnagUnity.Client::.cctor()
extern void Client__cctor_m3CB44602EE97E07D2276A7B64176947565F0AF7D (void);
// 0x00000028 System.Void BugsnagUnity.Client::<.ctor>b__47_0(System.Object,System.ComponentModel.PropertyChangedEventArgs)
extern void Client_U3C_ctorU3Eb__47_0_m6C7C10D22C7B96377F798AFF8861F43B37211AAD (void);
// 0x00000029 System.Void BugsnagUnity.Client/<RunInitialSessionCheck>d__68::.ctor(System.Int32)
extern void U3CRunInitialSessionCheckU3Ed__68__ctor_m35D94823F444DF69B573D75C07A7E36F517C3AD3 (void);
// 0x0000002A System.Void BugsnagUnity.Client/<RunInitialSessionCheck>d__68::System.IDisposable.Dispose()
extern void U3CRunInitialSessionCheckU3Ed__68_System_IDisposable_Dispose_mFEC0B489C21AAEEB37483E1C23B430B90341536E (void);
// 0x0000002B System.Boolean BugsnagUnity.Client/<RunInitialSessionCheck>d__68::MoveNext()
extern void U3CRunInitialSessionCheckU3Ed__68_MoveNext_m3E43F5C11C557DA47A643DA1B5CB400162781674 (void);
// 0x0000002C UnityEngine.AsyncOperation BugsnagUnity.Client/<RunInitialSessionCheck>d__68::System.Collections.Generic.IEnumerator<UnityEngine.AsyncOperation>.get_Current()
extern void U3CRunInitialSessionCheckU3Ed__68_System_Collections_Generic_IEnumeratorU3CUnityEngine_AsyncOperationU3E_get_Current_m2C895F0EBF0D853789AEF83719CF5F2ACFF2E249 (void);
// 0x0000002D System.Object BugsnagUnity.Client/<RunInitialSessionCheck>d__68::System.Collections.IEnumerator.get_Current()
extern void U3CRunInitialSessionCheckU3Ed__68_System_Collections_IEnumerator_get_Current_mED8A80EB01F06676CBA8E6AF859FE3BE67B55F0D (void);
// 0x0000002E System.Void BugsnagUnity.Configuration::.ctor(System.String)
extern void Configuration__ctor_mEFE60A6C2C34D6C2C9ABCDCCE229B1B593EDB83A (void);
// 0x0000002F System.Boolean BugsnagUnity.Configuration::get_ReportUncaughtExceptionsAsHandled()
extern void Configuration_get_ReportUncaughtExceptionsAsHandled_m42F8BA32514E01C6D516BC0DB19A064EA2A67C6C (void);
// 0x00000030 System.TimeSpan BugsnagUnity.Configuration::get_MaximumLogsTimePeriod()
extern void Configuration_get_MaximumLogsTimePeriod_m1C70BBE3B59893ED943C417A2CB08931FCA9EE0E (void);
// 0x00000031 System.Collections.Generic.Dictionary`2<UnityEngine.LogType,System.Int32> BugsnagUnity.Configuration::get_MaximumTypePerTimePeriod()
extern void Configuration_get_MaximumTypePerTimePeriod_mF8EE91F5AEF0D1CC7205C61DD1DA7EAC20F612A5 (void);
// 0x00000032 System.TimeSpan BugsnagUnity.Configuration::get_UniqueLogsTimePeriod()
extern void Configuration_get_UniqueLogsTimePeriod_m414F7AE05AC8534B70520E1F9EC31775A7658BB7 (void);
// 0x00000033 System.Void BugsnagUnity.Configuration::set_UniqueLogsTimePeriod(System.TimeSpan)
extern void Configuration_set_UniqueLogsTimePeriod_mF2BA9A11C2B0F8AB46FB5BA89EAC39C2A85DE36B (void);
// 0x00000034 UnityEngine.LogType BugsnagUnity.Configuration::get_BreadcrumbLogLevel()
extern void Configuration_get_BreadcrumbLogLevel_m1F55C4834EE645CB3CB41A42C15050DAEBD80C91 (void);
// 0x00000035 System.Boolean BugsnagUnity.Configuration::ShouldLeaveLogBreadcrumb(UnityEngine.LogType)
extern void Configuration_ShouldLeaveLogBreadcrumb_mE098E0F490561D477D048E497388F8C725938E63 (void);
// 0x00000036 BugsnagUnity.Payload.BreadcrumbType[] BugsnagUnity.Configuration::get_EnabledBreadcrumbTypes()
extern void Configuration_get_EnabledBreadcrumbTypes_m50D8B5DB11767E91488EF06341C8826E6F9928E9 (void);
// 0x00000037 System.Void BugsnagUnity.Configuration::set_EnabledBreadcrumbTypes(BugsnagUnity.Payload.BreadcrumbType[])
extern void Configuration_set_EnabledBreadcrumbTypes_mCA0B6FC29314F4C01B360E07F2356EBA864D9F35 (void);
// 0x00000038 System.Boolean BugsnagUnity.Configuration::IsBreadcrumbTypeEnabled(BugsnagUnity.Payload.BreadcrumbType)
extern void Configuration_IsBreadcrumbTypeEnabled_mFADFF1482D9C3A6AA38C53873A26CE36E863D72D (void);
// 0x00000039 System.String BugsnagUnity.Configuration::get_ApiKey()
extern void Configuration_get_ApiKey_m26FC6FA65B0DB4CD6A6F83B851FA2BB060C8B61C (void);
// 0x0000003A System.Void BugsnagUnity.Configuration::set_ApiKey(System.String)
extern void Configuration_set_ApiKey_mE624A03AF5FF8E9A69B49ACAD5CAD6DB3A3D284A (void);
// 0x0000003B System.Int32 BugsnagUnity.Configuration::get_MaximumBreadcrumbs()
extern void Configuration_get_MaximumBreadcrumbs_mEC7623D4D1FD10032EDC413E51749860332DD41C (void);
// 0x0000003C System.Void BugsnagUnity.Configuration::set_MaximumBreadcrumbs(System.Int32)
extern void Configuration_set_MaximumBreadcrumbs_mA17FF5791A622B71B44134BCD73D438BBDD4FA5F (void);
// 0x0000003D System.String BugsnagUnity.Configuration::get_ReleaseStage()
extern void Configuration_get_ReleaseStage_m2ED81ACD76A250975DC149FAFF3C73C13689E38A (void);
// 0x0000003E System.Void BugsnagUnity.Configuration::set_ReleaseStage(System.String)
extern void Configuration_set_ReleaseStage_mFF6C01E344B52E645DACAF467070B3C1B450D3AF (void);
// 0x0000003F System.String[] BugsnagUnity.Configuration::get_NotifyReleaseStages()
extern void Configuration_get_NotifyReleaseStages_mFABF309E753A66361C62D03872DFBD4A500FCB84 (void);
// 0x00000040 System.String BugsnagUnity.Configuration::get_AppVersion()
extern void Configuration_get_AppVersion_m8325FFC8A115BDC414E966B1A0EFE477DBC16DE4 (void);
// 0x00000041 System.Void BugsnagUnity.Configuration::set_AppVersion(System.String)
extern void Configuration_set_AppVersion_mD5AA59A59225D029B55A0E263ED31BAD5EAC5291 (void);
// 0x00000042 System.Uri BugsnagUnity.Configuration::get_Endpoint()
extern void Configuration_get_Endpoint_m60D0E7C89FF01F288A6DF3BC79B7BF1BDECF68E9 (void);
// 0x00000043 System.Void BugsnagUnity.Configuration::set_Endpoint(System.Uri)
extern void Configuration_set_Endpoint_m85162428CD2F2769B3461C7B392D08F0662F7BB9 (void);
// 0x00000044 System.String BugsnagUnity.Configuration::get_PayloadVersion()
extern void Configuration_get_PayloadVersion_m2FD9B1EC68DA61CE17F3088905ECB37D3650D88E (void);
// 0x00000045 System.Uri BugsnagUnity.Configuration::get_SessionEndpoint()
extern void Configuration_get_SessionEndpoint_m5352316D4BEC5740E59D3D7EA2D35B3496941E2A (void);
// 0x00000046 System.Void BugsnagUnity.Configuration::set_SessionEndpoint(System.Uri)
extern void Configuration_set_SessionEndpoint_m022BC04D458C87C9F05307789EB8EE47258D8BC0 (void);
// 0x00000047 System.String BugsnagUnity.Configuration::get_SessionPayloadVersion()
extern void Configuration_get_SessionPayloadVersion_m1AB4B1248611A5D81CBCEF09751F0E2695EFD6BE (void);
// 0x00000048 System.String BugsnagUnity.Configuration::get_Context()
extern void Configuration_get_Context_m690E11626353F43A6735E450872435FABBA51B97 (void);
// 0x00000049 System.Void BugsnagUnity.Configuration::set_Context(System.String)
extern void Configuration_set_Context_m277B0D1D31EB25B26C74AB78CEC8C99781FDD983 (void);
// 0x0000004A UnityEngine.LogType BugsnagUnity.Configuration::get_NotifyLogLevel()
extern void Configuration_get_NotifyLogLevel_m25ADEB5E47A46C5C325A032114E55727B94E9875 (void);
// 0x0000004B System.Void BugsnagUnity.Configuration::set_NotifyLogLevel(UnityEngine.LogType)
extern void Configuration_set_NotifyLogLevel_m389B240620FDFE1D178386E497F4EEC39207C00A (void);
// 0x0000004C System.Boolean BugsnagUnity.Configuration::get_AutoDetectErrors()
extern void Configuration_get_AutoDetectErrors_m3B17E05688FCCBCCF477B65C6A393277BBFE9FBD (void);
// 0x0000004D System.Void BugsnagUnity.Configuration::set_AutoDetectErrors(System.Boolean)
extern void Configuration_set_AutoDetectErrors_m57A6FD6CB79175D75C70DF4EC93F6F1225F57CBC (void);
// 0x0000004E System.Boolean BugsnagUnity.Configuration::get_AutoDetectAnrs()
extern void Configuration_get_AutoDetectAnrs_m11AF0DE8B003663CF0B02D9F89EAAC0CD294CD83 (void);
// 0x0000004F System.Void BugsnagUnity.Configuration::set_AutoDetectAnrs(System.Boolean)
extern void Configuration_set_AutoDetectAnrs_m864908E053D49180B4DD8EB8927317BBD302373F (void);
// 0x00000050 System.Boolean BugsnagUnity.Configuration::get_AutoTrackSessions()
extern void Configuration_get_AutoTrackSessions_m9AA5B23720E20ED899DC59FC1E6EE10F92277448 (void);
// 0x00000051 System.Void BugsnagUnity.Configuration::set_AutoTrackSessions(System.Boolean)
extern void Configuration_set_AutoTrackSessions_m09B00338DD907698D0BA384743BB75D6E6A9C7BB (void);
// 0x00000052 BugsnagUnity.LogTypeSeverityMapping BugsnagUnity.Configuration::get_LogTypeSeverityMapping()
extern void Configuration_get_LogTypeSeverityMapping_m827CAB5AD722C27F92FF35EB4C3CFAEBF6335282 (void);
// 0x00000053 System.String BugsnagUnity.Configuration::get_ScriptingBackend()
extern void Configuration_get_ScriptingBackend_mDCA23140873BD310F5BB3C0E1B9780A70B6A39DA (void);
// 0x00000054 System.Void BugsnagUnity.Configuration::set_ScriptingBackend(System.String)
extern void Configuration_set_ScriptingBackend_mFCECE1CA70A539C9D4F78388DB5B0D06F1219BE0 (void);
// 0x00000055 System.String BugsnagUnity.Configuration::get_DotnetScriptingRuntime()
extern void Configuration_get_DotnetScriptingRuntime_m44C9119F0325AD96533F3A91C80BE46E523C9672 (void);
// 0x00000056 System.Void BugsnagUnity.Configuration::set_DotnetScriptingRuntime(System.String)
extern void Configuration_set_DotnetScriptingRuntime_m6225859A8B86AA15181C49CEC129919C91AA52DE (void);
// 0x00000057 System.String BugsnagUnity.Configuration::get_DotnetApiCompatibility()
extern void Configuration_get_DotnetApiCompatibility_mA9F1AACD9C4A2DB7E15A3D9EE60E706F69DEB39D (void);
// 0x00000058 System.Void BugsnagUnity.Configuration::set_DotnetApiCompatibility(System.String)
extern void Configuration_set_DotnetApiCompatibility_mB37C275C6543E94D289A939F164103C3902B4E38 (void);
// 0x00000059 BugsnagUnity.ErrorTypes[] BugsnagUnity.Configuration::get_EnabledErrorTypes()
extern void Configuration_get_EnabledErrorTypes_mB43BD03300099E87056B8D2075DE5E6AD36B8624 (void);
// 0x0000005A System.Void BugsnagUnity.Configuration::set_EnabledErrorTypes(BugsnagUnity.ErrorTypes[])
extern void Configuration_set_EnabledErrorTypes_m17778B39D3715A62D26E16AF9430A6046EF763F7 (void);
// 0x0000005B System.Boolean BugsnagUnity.Configuration::IsErrorTypeEnabled(BugsnagUnity.ErrorTypes)
extern void Configuration_IsErrorTypeEnabled_m1F42300333B172C069C8D9F62840DF3918796E52 (void);
// 0x0000005C System.Boolean BugsnagUnity.Configuration::IsUnityLogErrorTypeEnabled(UnityEngine.LogType)
extern void Configuration_IsUnityLogErrorTypeEnabled_mEAF961167FBB5C9EABF2F32E97007B8C33C529A4 (void);
// 0x0000005D System.Boolean BugsnagUnity.Configuration::IsRunningInEditor()
extern void Configuration_IsRunningInEditor_m68E95F63E069EDDA3573171CD7D03B4061A4079B (void);
// 0x0000005E System.Void BugsnagUnity.IBreadcrumbs::Leave(System.String,BugsnagUnity.Payload.BreadcrumbType,System.Collections.Generic.IDictionary`2<System.String,System.String>)
// 0x0000005F System.Void BugsnagUnity.IBreadcrumbs::Leave(BugsnagUnity.Payload.Breadcrumb)
// 0x00000060 BugsnagUnity.Payload.Breadcrumb[] BugsnagUnity.IBreadcrumbs::Retrieve()
// 0x00000061 System.Void BugsnagUnity.Middleware::.ctor(System.Object,System.IntPtr)
extern void Middleware__ctor_m3BB553AA2794FBB0600E864A45251813E4384100 (void);
// 0x00000062 System.Void BugsnagUnity.Middleware::Invoke(BugsnagUnity.Payload.Report)
extern void Middleware_Invoke_m2ED3D48544E306A8B5CA1E5AF14643F7F36AF5C8 (void);
// 0x00000063 System.Void BugsnagUnity.IClient::SetApplicationState(System.Boolean)
// 0x00000064 System.String BugsnagUnity.IConfiguration::get_ApiKey()
// 0x00000065 System.Boolean BugsnagUnity.IConfiguration::get_ReportUncaughtExceptionsAsHandled()
// 0x00000066 System.Boolean BugsnagUnity.IConfiguration::IsUnityLogErrorTypeEnabled(UnityEngine.LogType)
// 0x00000067 System.Boolean BugsnagUnity.IConfiguration::IsErrorTypeEnabled(BugsnagUnity.ErrorTypes)
// 0x00000068 System.TimeSpan BugsnagUnity.IConfiguration::get_MaximumLogsTimePeriod()
// 0x00000069 BugsnagUnity.Payload.BreadcrumbType[] BugsnagUnity.IConfiguration::get_EnabledBreadcrumbTypes()
// 0x0000006A System.Boolean BugsnagUnity.IConfiguration::IsBreadcrumbTypeEnabled(BugsnagUnity.Payload.BreadcrumbType)
// 0x0000006B System.Boolean BugsnagUnity.IConfiguration::ShouldLeaveLogBreadcrumb(UnityEngine.LogType)
// 0x0000006C System.Collections.Generic.Dictionary`2<UnityEngine.LogType,System.Int32> BugsnagUnity.IConfiguration::get_MaximumTypePerTimePeriod()
// 0x0000006D System.TimeSpan BugsnagUnity.IConfiguration::get_UniqueLogsTimePeriod()
// 0x0000006E System.Int32 BugsnagUnity.IConfiguration::get_MaximumBreadcrumbs()
// 0x0000006F System.String BugsnagUnity.IConfiguration::get_ReleaseStage()
// 0x00000070 System.String[] BugsnagUnity.IConfiguration::get_NotifyReleaseStages()
// 0x00000071 System.String BugsnagUnity.IConfiguration::get_AppVersion()
// 0x00000072 System.Uri BugsnagUnity.IConfiguration::get_Endpoint()
// 0x00000073 System.String BugsnagUnity.IConfiguration::get_PayloadVersion()
// 0x00000074 System.Uri BugsnagUnity.IConfiguration::get_SessionEndpoint()
// 0x00000075 System.String BugsnagUnity.IConfiguration::get_SessionPayloadVersion()
// 0x00000076 System.String BugsnagUnity.IConfiguration::get_Context()
// 0x00000077 System.Void BugsnagUnity.IConfiguration::set_Context(System.String)
// 0x00000078 UnityEngine.LogType BugsnagUnity.IConfiguration::get_NotifyLogLevel()
// 0x00000079 System.Boolean BugsnagUnity.IConfiguration::get_AutoDetectErrors()
// 0x0000007A System.Boolean BugsnagUnity.IConfiguration::get_AutoDetectAnrs()
// 0x0000007B System.Boolean BugsnagUnity.IConfiguration::get_AutoTrackSessions()
// 0x0000007C BugsnagUnity.LogTypeSeverityMapping BugsnagUnity.IConfiguration::get_LogTypeSeverityMapping()
// 0x0000007D System.String BugsnagUnity.IConfiguration::get_ScriptingBackend()
// 0x0000007E System.String BugsnagUnity.IConfiguration::get_DotnetScriptingRuntime()
// 0x0000007F System.String BugsnagUnity.IConfiguration::get_DotnetApiCompatibility()
// 0x00000080 System.Void BugsnagUnity.IDelivery::Send(BugsnagUnity.Payload.IPayload)
// 0x00000081 System.Boolean BugsnagUnity.Delivery::get_DelayBeforeDelivery()
extern void Delivery_get_DelayBeforeDelivery_m7AB1F79274A73E685EC5A758B665B8BC7A04EC01 (void);
// 0x00000082 System.Void BugsnagUnity.Delivery::set_DelayBeforeDelivery(System.Boolean)
extern void Delivery_set_DelayBeforeDelivery_m63453B88D22082D9ADED5B763C1B6E7A2E72CF1B (void);
// 0x00000083 System.Threading.Thread BugsnagUnity.Delivery::get_Worker()
extern void Delivery_get_Worker_mEB9D498AA85A048FF2BA877582B999D5964220E2 (void);
// 0x00000084 BugsnagUnity.BlockingQueue`1<BugsnagUnity.Payload.IPayload> BugsnagUnity.Delivery::get_Queue()
extern void Delivery_get_Queue_mFEA2E06811E78184514BF11A70D5437D4215A9B5 (void);
// 0x00000085 UnityEngine.GameObject BugsnagUnity.Delivery::get_DispatcherObject()
extern void Delivery_get_DispatcherObject_m6E9F3F66C215E62D926C92C321424A24C8ACEAC4 (void);
// 0x00000086 System.Void BugsnagUnity.Delivery::.ctor()
extern void Delivery__ctor_mA2BD0D4F49A8C972142545833DD98AA8E16C5DBF (void);
// 0x00000087 System.Void BugsnagUnity.Delivery::ProcessQueue()
extern void Delivery_ProcessQueue_m6BCBDDA61F3AAA3131FF57A28719FFEF7D907090 (void);
// 0x00000088 System.Void BugsnagUnity.Delivery::SerializeAndDeliverPayload(BugsnagUnity.Payload.IPayload)
extern void Delivery_SerializeAndDeliverPayload_m6F8EAAE277D299F178B50229D6149214A92C6B9B (void);
// 0x00000089 System.Void BugsnagUnity.Delivery::Send(BugsnagUnity.Payload.IPayload)
extern void Delivery_Send_m14965AAB839443C0B29AE514E0A4F194ED022A8E (void);
// 0x0000008A System.Collections.IEnumerator BugsnagUnity.Delivery::PushToServer(BugsnagUnity.Payload.IPayload,System.Byte[])
extern void Delivery_PushToServer_mF9E95074FC95542194A64799755B3FCC3D3FE0B3 (void);
// 0x0000008B System.Boolean BugsnagUnity.Delivery::CanUseThreading()
extern void Delivery_CanUseThreading_m507BF1706697B2DCB2B583AE4366B5AA1FD65C5E (void);
// 0x0000008C System.Void BugsnagUnity.Delivery/<>c__DisplayClass17_0::.ctor()
extern void U3CU3Ec__DisplayClass17_0__ctor_m89C13BFC27C778657C3962CB8E1C9B616572167B (void);
// 0x0000008D System.Void BugsnagUnity.Delivery/<>c__DisplayClass17_0::<Send>b__0()
extern void U3CU3Ec__DisplayClass17_0_U3CSendU3Eb__0_mB895F5F9E63EBAB224BF1C7AE34CCCE42BFFFE6C (void);
// 0x0000008E System.Void BugsnagUnity.Delivery/<PushToServer>d__18::.ctor(System.Int32)
extern void U3CPushToServerU3Ed__18__ctor_m75CC2D5EDF2C54BAB639566CE255A6485473CC99 (void);
// 0x0000008F System.Void BugsnagUnity.Delivery/<PushToServer>d__18::System.IDisposable.Dispose()
extern void U3CPushToServerU3Ed__18_System_IDisposable_Dispose_m5C6E06B4353F2DE37D004A9D84CE8E0B941766E3 (void);
// 0x00000090 System.Boolean BugsnagUnity.Delivery/<PushToServer>d__18::MoveNext()
extern void U3CPushToServerU3Ed__18_MoveNext_mC8215BD2F78080EFBA17F44E6D4EB13BF3398541 (void);
// 0x00000091 System.Void BugsnagUnity.Delivery/<PushToServer>d__18::<>m__Finally1()
extern void U3CPushToServerU3Ed__18_U3CU3Em__Finally1_mADEB2F11733AE86975A8CDC7635DFBF1F6957039 (void);
// 0x00000092 System.Object BugsnagUnity.Delivery/<PushToServer>d__18::System.Collections.Generic.IEnumerator<System.Object>.get_Current()
extern void U3CPushToServerU3Ed__18_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m34D2BDDEFF50C11D52630D7578729247871C7402 (void);
// 0x00000093 System.Object BugsnagUnity.Delivery/<PushToServer>d__18::System.Collections.IEnumerator.get_Current()
extern void U3CPushToServerU3Ed__18_System_Collections_IEnumerator_get_Current_mAA0C726381E2D1DC1A8FF82DFEE3710FE1DF4821 (void);
// 0x00000094 BugsnagUnity.IConfiguration BugsnagUnity.INativeClient::get_Configuration()
// 0x00000095 BugsnagUnity.IBreadcrumbs BugsnagUnity.INativeClient::get_Breadcrumbs()
// 0x00000096 BugsnagUnity.IDelivery BugsnagUnity.INativeClient::get_Delivery()
// 0x00000097 System.Void BugsnagUnity.INativeClient::PopulateApp(BugsnagUnity.Payload.App)
// 0x00000098 System.Void BugsnagUnity.INativeClient::PopulateDevice(BugsnagUnity.Payload.Device)
// 0x00000099 System.Void BugsnagUnity.INativeClient::SetMetadata(System.String,System.Collections.Generic.Dictionary`2<System.String,System.String>)
// 0x0000009A System.Void BugsnagUnity.INativeClient::SetSession(BugsnagUnity.Payload.Session)
// 0x0000009B System.Void BugsnagUnity.INativeClient::SetUser(BugsnagUnity.Payload.User)
// 0x0000009C System.Void BugsnagUnity.INativeClient::PopulateUser(BugsnagUnity.Payload.User)
// 0x0000009D System.Void BugsnagUnity.INativeClient::PopulateMetadata(BugsnagUnity.Payload.Metadata)
// 0x0000009E System.Void BugsnagUnity.INativeClient::SetContext(System.String)
// 0x0000009F System.Collections.Generic.Dictionary`2<UnityEngine.LogType,BugsnagUnity.Severity> BugsnagUnity.LogTypeSeverityMapping::get_Mappings()
extern void LogTypeSeverityMapping_get_Mappings_mA0DDF8E118E1BE5EAC7B8E16EA861053B5C493D7 (void);
// 0x000000A0 System.Void BugsnagUnity.LogTypeSeverityMapping::.ctor()
extern void LogTypeSeverityMapping__ctor_m1D5EA040110BBFF38E773B2A4F1632037BF2FAA9 (void);
// 0x000000A1 BugsnagUnity.Severity BugsnagUnity.LogTypeSeverityMapping::Map(UnityEngine.LogType)
extern void LogTypeSeverityMapping_Map_m32F8FC35B72949C934CB6EB36DB2591014EE2F91 (void);
// 0x000000A2 System.Void BugsnagUnity.MainThreadDispatchBehaviour::Update()
extern void MainThreadDispatchBehaviour_Update_m8AE38A5667F2C9529029E619708D50588B3092C9 (void);
// 0x000000A3 System.Void BugsnagUnity.MainThreadDispatchBehaviour::Enqueue(System.Collections.IEnumerator)
extern void MainThreadDispatchBehaviour_Enqueue_m6625876BA5117695B5A0DF2917BC769BEBAF99D4 (void);
// 0x000000A4 System.Void BugsnagUnity.MainThreadDispatchBehaviour::Enqueue(System.Action)
extern void MainThreadDispatchBehaviour_Enqueue_mD927EED4B8A04F4DB74A332BEF0A5E1FB4FB6A89 (void);
// 0x000000A5 System.Collections.IEnumerator BugsnagUnity.MainThreadDispatchBehaviour::ActionWrapper(System.Action)
extern void MainThreadDispatchBehaviour_ActionWrapper_mB5C0584FF9C18465CD557F9162AF9EB0BCDA188A (void);
// 0x000000A6 System.Void BugsnagUnity.MainThreadDispatchBehaviour::EnqueueWithDelayCoroutine(System.Action,System.Single)
extern void MainThreadDispatchBehaviour_EnqueueWithDelayCoroutine_mD8D1DAFCF24647B6FE3FD3968DE8610CDD195295 (void);
// 0x000000A7 System.Collections.IEnumerator BugsnagUnity.MainThreadDispatchBehaviour::DelayAction(System.Action,System.Single)
extern void MainThreadDispatchBehaviour_DelayAction_m6BB6F287E896D420F99D8B0082661BDFA621A4BF (void);
// 0x000000A8 System.Boolean BugsnagUnity.MainThreadDispatchBehaviour::Exists()
extern void MainThreadDispatchBehaviour_Exists_m706931ADACE843B39890B5D10D72890BCE7CF195 (void);
// 0x000000A9 BugsnagUnity.MainThreadDispatchBehaviour BugsnagUnity.MainThreadDispatchBehaviour::Instance()
extern void MainThreadDispatchBehaviour_Instance_m9712AFBA67F78D4D1C6AB89D690F68F2C190E23A (void);
// 0x000000AA System.Void BugsnagUnity.MainThreadDispatchBehaviour::Awake()
extern void MainThreadDispatchBehaviour_Awake_mAD7FF9C5702E9D4B4F9D18D51602481C91E51E80 (void);
// 0x000000AB System.Void BugsnagUnity.MainThreadDispatchBehaviour::OnDestroy()
extern void MainThreadDispatchBehaviour_OnDestroy_mC3386A0C3A5F74CCAED5F8253F73ED2B46295E07 (void);
// 0x000000AC System.Void BugsnagUnity.MainThreadDispatchBehaviour::.ctor()
extern void MainThreadDispatchBehaviour__ctor_m3C671510C883A1FE8B2FCDEFD8E2DDE58873AC8A (void);
// 0x000000AD System.Void BugsnagUnity.MainThreadDispatchBehaviour::.cctor()
extern void MainThreadDispatchBehaviour__cctor_m4D158A2F4611329798433E94763728F2EB40B243 (void);
// 0x000000AE System.Void BugsnagUnity.MainThreadDispatchBehaviour/<>c__DisplayClass2_0::.ctor()
extern void U3CU3Ec__DisplayClass2_0__ctor_m498D8D34BAD6E1F6DE580D599931FFA4D7EA345E (void);
// 0x000000AF System.Void BugsnagUnity.MainThreadDispatchBehaviour/<>c__DisplayClass2_0::<Enqueue>b__0()
extern void U3CU3Ec__DisplayClass2_0_U3CEnqueueU3Eb__0_m6C4CBB5C27D4007842192852EC18900CBDAE870A (void);
// 0x000000B0 System.Void BugsnagUnity.MainThreadDispatchBehaviour/<ActionWrapper>d__4::.ctor(System.Int32)
extern void U3CActionWrapperU3Ed__4__ctor_m7FBD090B1412538A4F11F3A6CF9C315713F44A03 (void);
// 0x000000B1 System.Void BugsnagUnity.MainThreadDispatchBehaviour/<ActionWrapper>d__4::System.IDisposable.Dispose()
extern void U3CActionWrapperU3Ed__4_System_IDisposable_Dispose_mEFEB52C27A5C6DE82B140A52D5604027E09DD52D (void);
// 0x000000B2 System.Boolean BugsnagUnity.MainThreadDispatchBehaviour/<ActionWrapper>d__4::MoveNext()
extern void U3CActionWrapperU3Ed__4_MoveNext_m0FE20ED2B50867F59AAD87DBAD39EB2B37FE06AC (void);
// 0x000000B3 System.Object BugsnagUnity.MainThreadDispatchBehaviour/<ActionWrapper>d__4::System.Collections.Generic.IEnumerator<System.Object>.get_Current()
extern void U3CActionWrapperU3Ed__4_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m07B3B678F9311BA8B3D57A8C7560B07347C8372D (void);
// 0x000000B4 System.Object BugsnagUnity.MainThreadDispatchBehaviour/<ActionWrapper>d__4::System.Collections.IEnumerator.get_Current()
extern void U3CActionWrapperU3Ed__4_System_Collections_IEnumerator_get_Current_mD6356946C7E7F982F72A729589B4CEBF50729420 (void);
// 0x000000B5 System.Void BugsnagUnity.MainThreadDispatchBehaviour/<DelayAction>d__6::.ctor(System.Int32)
extern void U3CDelayActionU3Ed__6__ctor_m5A23537B710958B9D29EF34F395EEDCFA1EB3FB5 (void);
// 0x000000B6 System.Void BugsnagUnity.MainThreadDispatchBehaviour/<DelayAction>d__6::System.IDisposable.Dispose()
extern void U3CDelayActionU3Ed__6_System_IDisposable_Dispose_m62BC62F4C37FADF2E03DC0796D385D4292B83DAB (void);
// 0x000000B7 System.Boolean BugsnagUnity.MainThreadDispatchBehaviour/<DelayAction>d__6::MoveNext()
extern void U3CDelayActionU3Ed__6_MoveNext_mF2D598CD70803BECE9890FD521FBB6A345277955 (void);
// 0x000000B8 System.Object BugsnagUnity.MainThreadDispatchBehaviour/<DelayAction>d__6::System.Collections.Generic.IEnumerator<System.Object>.get_Current()
extern void U3CDelayActionU3Ed__6_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m8B0C243749E3B04ED21C796F6D30269A98FF26D3 (void);
// 0x000000B9 System.Object BugsnagUnity.MainThreadDispatchBehaviour/<DelayAction>d__6::System.Collections.IEnumerator.get_Current()
extern void U3CDelayActionU3Ed__6_System_Collections_IEnumerator_get_Current_mE56724F0F2D14816F98821EDF4397EE59758F7B4 (void);
// 0x000000BA BugsnagUnity.IConfiguration BugsnagUnity.MaximumLogTypeCounter::get_Configuration()
extern void MaximumLogTypeCounter_get_Configuration_m0EB736B3D884A30AA1053E6AFCF707B4C130DD34 (void);
// 0x000000BB System.Collections.Generic.Dictionary`2<UnityEngine.LogType,System.Int32> BugsnagUnity.MaximumLogTypeCounter::get_CurrentCounts()
extern void MaximumLogTypeCounter_get_CurrentCounts_m086A5330C326EF35A1CCCABF59EFC33A26277CAE (void);
// 0x000000BC System.DateTime BugsnagUnity.MaximumLogTypeCounter::get_FlushAt()
extern void MaximumLogTypeCounter_get_FlushAt_m622366BEA58B2431B9AD8BDA8537BB8CB46C5BFB (void);
// 0x000000BD System.Void BugsnagUnity.MaximumLogTypeCounter::set_FlushAt(System.DateTime)
extern void MaximumLogTypeCounter_set_FlushAt_m620CA5FB4946B6C6CCEA46C9B61B25B06319DA4C (void);
// 0x000000BE System.TimeSpan BugsnagUnity.MaximumLogTypeCounter::get_MaximumLogsTimePeriod()
extern void MaximumLogTypeCounter_get_MaximumLogsTimePeriod_mBF7BBF11CE0786A5E388B0099485F37A16035FE1 (void);
// 0x000000BF System.Collections.Generic.Dictionary`2<UnityEngine.LogType,System.Int32> BugsnagUnity.MaximumLogTypeCounter::get_MaximumTypePerTimePeriod()
extern void MaximumLogTypeCounter_get_MaximumTypePerTimePeriod_m2C89C6DF598FA6090F520FE78DAAEB341478C073 (void);
// 0x000000C0 System.Void BugsnagUnity.MaximumLogTypeCounter::.ctor(BugsnagUnity.IConfiguration)
extern void MaximumLogTypeCounter__ctor_m2EDF753E06CD2254F8AAFF05CE04202BC971879E (void);
// 0x000000C1 System.Boolean BugsnagUnity.MaximumLogTypeCounter::ShouldSend(BugsnagUnity.UnityLogMessage)
extern void MaximumLogTypeCounter_ShouldSend_m9D9950070917B55549C71868F3EB60CB0E4935D1 (void);
// 0x000000C2 System.Void BugsnagUnity.ISessionTracker::StartSession()
// 0x000000C3 BugsnagUnity.Payload.Session BugsnagUnity.ISessionTracker::get_CurrentSession()
// 0x000000C4 System.Void BugsnagUnity.ISessionTracker::AddException(BugsnagUnity.Payload.Report)
// 0x000000C5 BugsnagUnity.Client BugsnagUnity.SessionTracker::get_Client()
extern void SessionTracker_get_Client_m761307B966EB7FFB8DFD0295780722D739DC8FAA (void);
// 0x000000C6 BugsnagUnity.Payload.Session BugsnagUnity.SessionTracker::get_CurrentSession()
extern void SessionTracker_get_CurrentSession_m4922443B6C0BDCBA6956B654CAEA86D382A5CB3F (void);
// 0x000000C7 System.Void BugsnagUnity.SessionTracker::set_CurrentSession(BugsnagUnity.Payload.Session)
extern void SessionTracker_set_CurrentSession_m982E6605E9DECAB258CB7F7B4FFC6B670F939B27 (void);
// 0x000000C8 System.Void BugsnagUnity.SessionTracker::.ctor(BugsnagUnity.Client)
extern void SessionTracker__ctor_m370C865FAE59D4B7775BA1A79A297DB3B3CFCE7F (void);
// 0x000000C9 System.Void BugsnagUnity.SessionTracker::StartSession()
extern void SessionTracker_StartSession_m13B4C0D7F8B01101B1D7FAB7684A344EA15157F1 (void);
// 0x000000CA System.Void BugsnagUnity.SessionTracker::AddException(BugsnagUnity.Payload.Report)
extern void SessionTracker_AddException_mC6E66C4881535DD43AE97D57DCD8C53288678F27 (void);
// 0x000000CB System.Collections.Generic.Dictionary`2<UnityEngine.LogType,System.Int32> BugsnagUnity.LogTypeExtensions::get_LogTypeMapping()
extern void LogTypeExtensions_get_LogTypeMapping_m3EAEB788995EB06E806855955D061ADE741309C3 (void);
// 0x000000CC System.Boolean BugsnagUnity.LogTypeExtensions::IsGreaterThanOrEqualTo(UnityEngine.LogType,UnityEngine.LogType)
extern void LogTypeExtensions_IsGreaterThanOrEqualTo_m0299C5A6D21DCBD0065C04B3FDE2C77607A6F249 (void);
// 0x000000CD System.Void BugsnagUnity.LogTypeExtensions::.cctor()
extern void LogTypeExtensions__cctor_m1AB3FA8BD5414EAC9494A2C1B9E82D46F0D3ECD0 (void);
// 0x000000CE System.Boolean BugsnagUnity.LogTypeExtensions/LogTypeComparer::Equals(UnityEngine.LogType,UnityEngine.LogType)
extern void LogTypeComparer_Equals_m0C42DF502C711B8655859CBDB4ADFC022287852A (void);
// 0x000000CF System.Int32 BugsnagUnity.LogTypeExtensions/LogTypeComparer::GetHashCode(UnityEngine.LogType)
extern void LogTypeComparer_GetHashCode_m6A3EA1CF1D693A2BBEC09FC95F7C27C0C176D5A1 (void);
// 0x000000D0 System.Void BugsnagUnity.LogTypeExtensions/LogTypeComparer::.ctor()
extern void LogTypeComparer__ctor_m7D9E271202D5E7BAAF0D808AAE8D6508BDD45CBD (void);
// 0x000000D1 System.Void BugsnagUnity.JsonObject::.ctor()
extern void JsonObject__ctor_m8715AD972E621AF25134CC64D9B2F8F93629F6FB (void);
// 0x000000D2 System.Void BugsnagUnity.JsonObject::Add(System.String,System.Object)
extern void JsonObject_Add_m77AC615EEE88B11CF1C203D72EDF5636E88EB65E (void);
// 0x000000D3 System.Boolean BugsnagUnity.JsonObject::ContainsKey(System.String)
extern void JsonObject_ContainsKey_m935805209E5750BBFA73C7DF0E87A063B0D41383 (void);
// 0x000000D4 System.Collections.Generic.ICollection`1<System.String> BugsnagUnity.JsonObject::get_Keys()
extern void JsonObject_get_Keys_m31090FCD83130C0899EB173A89128FA9BD05E364 (void);
// 0x000000D5 System.Boolean BugsnagUnity.JsonObject::Remove(System.String)
extern void JsonObject_Remove_m827C0CCAA18438DAA0810D51F22E98FFCE5CCD9D (void);
// 0x000000D6 System.Collections.Generic.ICollection`1<System.Object> BugsnagUnity.JsonObject::get_Values()
extern void JsonObject_get_Values_m0F60717DFC5A56BA64268B0E93984272FADD68FB (void);
// 0x000000D7 System.Object BugsnagUnity.JsonObject::get_Item(System.String)
extern void JsonObject_get_Item_m3AC2A84F3684E05D102EACE56BAB32ADFA69E56B (void);
// 0x000000D8 System.Void BugsnagUnity.JsonObject::set_Item(System.String,System.Object)
extern void JsonObject_set_Item_mAC4E4BA730E000AED0ACAC635853A3F8EB35008B (void);
// 0x000000D9 System.Void BugsnagUnity.JsonObject::Add(System.Collections.Generic.KeyValuePair`2<System.String,System.Object>)
extern void JsonObject_Add_m359F65AF8844A6070E776393C60062B67D1820A2 (void);
// 0x000000DA System.Void BugsnagUnity.JsonObject::Clear()
extern void JsonObject_Clear_m59B6F4A7F6D4E15FCCEDCF9B0B7DE692EC3838CC (void);
// 0x000000DB System.Boolean BugsnagUnity.JsonObject::Contains(System.Collections.Generic.KeyValuePair`2<System.String,System.Object>)
extern void JsonObject_Contains_m05C4DF4EBB2B7E0C4A0E9A9CA975A81EFB8A0603 (void);
// 0x000000DC System.Void BugsnagUnity.JsonObject::CopyTo(System.Collections.Generic.KeyValuePair`2<System.String,System.Object>[],System.Int32)
extern void JsonObject_CopyTo_m8758B9F75C0B3F2B92EF9FD44966CFD783FD536F (void);
// 0x000000DD System.Int32 BugsnagUnity.JsonObject::get_Count()
extern void JsonObject_get_Count_m5FC4D511D235E7BAFC1EA295625BE375E55105EB (void);
// 0x000000DE System.Boolean BugsnagUnity.JsonObject::get_IsReadOnly()
extern void JsonObject_get_IsReadOnly_m2CA994EC39DC8198468E98410E7B3329C1DD998E (void);
// 0x000000DF System.Boolean BugsnagUnity.JsonObject::Remove(System.Collections.Generic.KeyValuePair`2<System.String,System.Object>)
extern void JsonObject_Remove_m16B5758DAE38725910BA1AF0C922099074323E9E (void);
// 0x000000E0 System.Collections.Generic.IEnumerator`1<System.Collections.Generic.KeyValuePair`2<System.String,System.Object>> BugsnagUnity.JsonObject::GetEnumerator()
extern void JsonObject_GetEnumerator_m8A96038B1227596B01A7A4E5CD12BC6FFCB3B1D7 (void);
// 0x000000E1 System.Collections.IEnumerator BugsnagUnity.JsonObject::System.Collections.IEnumerable.GetEnumerator()
extern void JsonObject_System_Collections_IEnumerable_GetEnumerator_m4666FA0C1809B612450A596004EE7150F54BE16A (void);
// 0x000000E2 System.Void BugsnagUnity.SimpleJson::.cctor()
extern void SimpleJson__cctor_m8B0C6436294D917930ED85E06286DCBDB66C2091 (void);
// 0x000000E3 System.Void BugsnagUnity.SimpleJson::SerializeObject(System.Object,System.IO.StreamWriter,BugsnagUnity.IJsonSerializerStrategy,System.Collections.IDictionary,System.Collections.IDictionary)
extern void SimpleJson_SerializeObject_m7ADC508716448076C086A159E488F054D1CDADD2 (void);
// 0x000000E4 System.Void BugsnagUnity.SimpleJson::SerializeObject(System.Object,System.IO.StreamWriter)
extern void SimpleJson_SerializeObject_mB250869D7286B5CF6077499E4E50A6D91A02EEE6 (void);
// 0x000000E5 System.Void BugsnagUnity.SimpleJson::SerializeObject(System.Object,System.IO.StreamWriter,System.Collections.IDictionary)
extern void SimpleJson_SerializeObject_m5D990CDBCEA328A76B5BEB29373DAC9FC5C795DF (void);
// 0x000000E6 System.Boolean BugsnagUnity.SimpleJson::SerializeValue(BugsnagUnity.IJsonSerializerStrategy,System.Object,System.IO.StreamWriter,System.Collections.IDictionary,System.Collections.IDictionary,System.Boolean)
extern void SimpleJson_SerializeValue_mE10B74D19B1C3D0BA877E8305DE901BF8DF71342 (void);
// 0x000000E7 System.Boolean BugsnagUnity.SimpleJson::SerializeObject(BugsnagUnity.IJsonSerializerStrategy,System.Collections.IEnumerable,System.Collections.IEnumerable,System.IO.StreamWriter,System.Collections.IDictionary,System.Collections.IDictionary,System.Boolean)
extern void SimpleJson_SerializeObject_m0898B20C1FDD514EA0BA161BCB14BDA3EC2EEB76 (void);
// 0x000000E8 System.Boolean BugsnagUnity.SimpleJson::SerializeArray(BugsnagUnity.IJsonSerializerStrategy,System.Collections.IEnumerable,System.IO.StreamWriter,System.Collections.IDictionary,System.Collections.IDictionary,System.Boolean)
extern void SimpleJson_SerializeArray_m804F1DD3C44E69291898252B9C3CD79359299E16 (void);
// 0x000000E9 System.Boolean BugsnagUnity.SimpleJson::SerializeString(System.String,System.IO.StreamWriter)
extern void SimpleJson_SerializeString_m28B03126411582CEA245530DF3D94DBDE8A18AE6 (void);
// 0x000000EA System.Void BugsnagUnity.SimpleJson::IntToHex(System.Int32,System.Char[])
extern void SimpleJson_IntToHex_m3E699B008E7588762A1404B6D8BFC95EF7AC7154 (void);
// 0x000000EB System.Boolean BugsnagUnity.SimpleJson::SerializeNumber(System.Object,System.IO.StreamWriter)
extern void SimpleJson_SerializeNumber_m10B97CA6135D5F1C04537ECDA55E9864832B3292 (void);
// 0x000000EC BugsnagUnity.IJsonSerializerStrategy BugsnagUnity.SimpleJson::get_CurrentJsonSerializerStrategy()
extern void SimpleJson_get_CurrentJsonSerializerStrategy_m859209D40C5C747B3E11933FBF6E98455F677383 (void);
// 0x000000ED BugsnagUnity.PocoJsonSerializerStrategy BugsnagUnity.SimpleJson::get_PocoJsonSerializerStrategy()
extern void SimpleJson_get_PocoJsonSerializerStrategy_m97ECF185A0D9D054262ABAEF1A3051A62D4FCBAA (void);
// 0x000000EE System.Boolean BugsnagUnity.IJsonSerializerStrategy::TrySerializeNonPrimitiveObject(System.Object,System.Object&)
// 0x000000EF System.Void BugsnagUnity.PocoJsonSerializerStrategy::.ctor()
extern void PocoJsonSerializerStrategy__ctor_mF9867692309E8D15D647BF4B4E712423177A4281 (void);
// 0x000000F0 System.String BugsnagUnity.PocoJsonSerializerStrategy::MapClrMemberNameToJsonFieldName(System.String)
extern void PocoJsonSerializerStrategy_MapClrMemberNameToJsonFieldName_m5DBF53E4E2632398CB1EF943147DCAC6E673733E (void);
// 0x000000F1 BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate BugsnagUnity.PocoJsonSerializerStrategy::ContructorDelegateFactory(System.Type)
extern void PocoJsonSerializerStrategy_ContructorDelegateFactory_mFF425F030AD66F7F3AD71EA4B4F474DE0B6A71FC (void);
// 0x000000F2 System.Collections.Generic.IDictionary`2<System.String,BugsnagUnity.Reflection.ReflectionUtils/GetDelegate> BugsnagUnity.PocoJsonSerializerStrategy::GetterValueFactory(System.Type)
extern void PocoJsonSerializerStrategy_GetterValueFactory_m092F20D381AE5CB587396E1282CB298D70C5F4E4 (void);
// 0x000000F3 System.Collections.Generic.IDictionary`2<System.String,System.Collections.Generic.KeyValuePair`2<System.Type,BugsnagUnity.Reflection.ReflectionUtils/SetDelegate>> BugsnagUnity.PocoJsonSerializerStrategy::SetterValueFactory(System.Type)
extern void PocoJsonSerializerStrategy_SetterValueFactory_m5A1C6528DF48547AFDCDEDD9C573B24458C2F3A7 (void);
// 0x000000F4 System.Boolean BugsnagUnity.PocoJsonSerializerStrategy::TrySerializeNonPrimitiveObject(System.Object,System.Object&)
extern void PocoJsonSerializerStrategy_TrySerializeNonPrimitiveObject_m90D2213CFC5284F7152F3F5E5F4024B59C650C75 (void);
// 0x000000F5 System.Object BugsnagUnity.PocoJsonSerializerStrategy::SerializeEnum(System.Enum)
extern void PocoJsonSerializerStrategy_SerializeEnum_m62F3D96F3D835460CB276705DFA6FBA16919FE00 (void);
// 0x000000F6 System.Boolean BugsnagUnity.PocoJsonSerializerStrategy::TrySerializeKnownTypes(System.Object,System.Object&)
extern void PocoJsonSerializerStrategy_TrySerializeKnownTypes_m742A2933A309DAE3150D794C8A7D3F399A23446D (void);
// 0x000000F7 System.Boolean BugsnagUnity.PocoJsonSerializerStrategy::TrySerializeUnknownTypes(System.Object,System.Object&)
extern void PocoJsonSerializerStrategy_TrySerializeUnknownTypes_m364F17929AFA8801F43904C91C0E72DF80EC3278 (void);
// 0x000000F8 System.Void BugsnagUnity.PocoJsonSerializerStrategy::.cctor()
extern void PocoJsonSerializerStrategy__cctor_mD269A0BC11AE27E0C5192CE54DFCD1D9A1569B1D (void);
// 0x000000F9 System.Void BugsnagUnity.TimingTrackerBehaviour::Awake()
extern void TimingTrackerBehaviour_Awake_m37BD6D7AA1299E19CCA6A503193F932D1878B507 (void);
// 0x000000FA System.Void BugsnagUnity.TimingTrackerBehaviour::OnApplicationFocus(System.Boolean)
extern void TimingTrackerBehaviour_OnApplicationFocus_mE9B711B9033367FEDA4794ED6BE00392495D5EEA (void);
// 0x000000FB System.Void BugsnagUnity.TimingTrackerBehaviour::OnApplicationPause(System.Boolean)
extern void TimingTrackerBehaviour_OnApplicationPause_m02590C1A4ED24BB3AB2443A3A54C1972A8968816 (void);
// 0x000000FC System.Void BugsnagUnity.TimingTrackerBehaviour::.ctor()
extern void TimingTrackerBehaviour__ctor_m6DE6C100133CF39757C663D7537270F45CE12F18 (void);
// 0x000000FD System.String BugsnagUnity.TypeNameHelper::GetTypeDisplayName(System.Type,System.Boolean,System.Boolean)
extern void TypeNameHelper_GetTypeDisplayName_mD3E99C6C6925155C373E1DC823339B772D365D2F (void);
// 0x000000FE System.Void BugsnagUnity.TypeNameHelper::ProcessType(System.Text.StringBuilder,System.Type,BugsnagUnity.TypeNameHelper/DisplayNameOptions)
extern void TypeNameHelper_ProcessType_mE00F793FAE23AC3539ACA6C55EC7FFC1A9FA2423 (void);
// 0x000000FF System.Void BugsnagUnity.TypeNameHelper::ProcessArrayType(System.Text.StringBuilder,System.Type,BugsnagUnity.TypeNameHelper/DisplayNameOptions)
extern void TypeNameHelper_ProcessArrayType_m6DAA3B80B656C34A7F43567E88B0504F3D928BD7 (void);
// 0x00000100 System.Void BugsnagUnity.TypeNameHelper::ProcessGenericType(System.Text.StringBuilder,System.Type,System.Type[],System.Int32,BugsnagUnity.TypeNameHelper/DisplayNameOptions)
extern void TypeNameHelper_ProcessGenericType_m6199373973ECD0B0A1C46DAAC0A3247977CF2333 (void);
// 0x00000101 System.Void BugsnagUnity.TypeNameHelper::.cctor()
extern void TypeNameHelper__cctor_m111125B22DAC9E6D2D17B2DC5FC9D07BE31E5825 (void);
// 0x00000102 System.Void BugsnagUnity.TypeNameHelper/DisplayNameOptions::.ctor(System.Boolean,System.Boolean)
extern void DisplayNameOptions__ctor_mAA8EBA9CC2C017D61DE68D885761634143F8E4B8 (void);
// 0x00000103 System.Boolean BugsnagUnity.TypeNameHelper/DisplayNameOptions::get_FullName()
extern void DisplayNameOptions_get_FullName_m6F83DDB661DBEEDF4D7370998D35E1CD33D112FD (void);
// 0x00000104 System.Boolean BugsnagUnity.TypeNameHelper/DisplayNameOptions::get_IncludeGenericParameterNames()
extern void DisplayNameOptions_get_IncludeGenericParameterNames_m2F98661BD8E5EE7BBDD878D1CC76A17484CE939D (void);
// 0x00000105 System.Collections.Generic.Dictionary`2<BugsnagUnity.UnityLogMessage,System.Int32> BugsnagUnity.UniqueLogThrottle::get_Counter()
extern void UniqueLogThrottle_get_Counter_m66D5FE6EF0EB844457A3392DB04C44118CBE3CCC (void);
// 0x00000106 System.DateTime BugsnagUnity.UniqueLogThrottle::get_FlushAt()
extern void UniqueLogThrottle_get_FlushAt_m76C994BCC40D5113FDA528CE962CC4C71C5F9613 (void);
// 0x00000107 System.Void BugsnagUnity.UniqueLogThrottle::set_FlushAt(System.DateTime)
extern void UniqueLogThrottle_set_FlushAt_mAB8D168D035958FD9FD8C834ABCD396508C94BC5 (void);
// 0x00000108 BugsnagUnity.IConfiguration BugsnagUnity.UniqueLogThrottle::get_Configuration()
extern void UniqueLogThrottle_get_Configuration_mAA388C09A356092A891F16BC661403F995227FE2 (void);
// 0x00000109 System.TimeSpan BugsnagUnity.UniqueLogThrottle::get_UniqueLogsTimePeriod()
extern void UniqueLogThrottle_get_UniqueLogsTimePeriod_mDB0DC8D80E795CD820DC17AFBBBB593E3FBF9617 (void);
// 0x0000010A System.Void BugsnagUnity.UniqueLogThrottle::.ctor(BugsnagUnity.IConfiguration)
extern void UniqueLogThrottle__ctor_mE334191510C60134778C189E2944D279521EDEEF (void);
// 0x0000010B System.Boolean BugsnagUnity.UniqueLogThrottle::ShouldSend(BugsnagUnity.UnityLogMessage)
extern void UniqueLogThrottle_ShouldSend_m1CCB7C79E66F43FB2135831EBA0830B385A913B4 (void);
// 0x0000010C System.Boolean BugsnagUnity.UniqueLogThrottle/UnityLogMessageEqualityComparer::Equals(BugsnagUnity.UnityLogMessage,BugsnagUnity.UnityLogMessage)
extern void UnityLogMessageEqualityComparer_Equals_mDF590786C5D2192263FE8C51E8F56A97707CB837 (void);
// 0x0000010D System.Int32 BugsnagUnity.UniqueLogThrottle/UnityLogMessageEqualityComparer::GetHashCode(BugsnagUnity.UnityLogMessage)
extern void UnityLogMessageEqualityComparer_GetHashCode_m30D74C0D772B6CD9AFCD041ACDF4862AF815CCF0 (void);
// 0x0000010E System.Void BugsnagUnity.UniqueLogThrottle/UnityLogMessageEqualityComparer::.ctor()
extern void UnityLogMessageEqualityComparer__ctor_mC1AB1A9B8A286A1594DE740B1A7040029FC23723 (void);
// 0x0000010F System.Void BugsnagUnity.UnityLogMessage::.ctor(System.String,System.String,UnityEngine.LogType)
extern void UnityLogMessage__ctor_mBB64CD8A956F91FA2E36D37AAACB1F00AA6C10B0 (void);
// 0x00000110 System.String BugsnagUnity.UnityLogMessage::get_Condition()
extern void UnityLogMessage_get_Condition_mB46AE016A06998547F14B993E8A5D5DEF6D6E4F7 (void);
// 0x00000111 System.String BugsnagUnity.UnityLogMessage::get_StackTrace()
extern void UnityLogMessage_get_StackTrace_mB876FA320B6359901B52F1DD95B0A4149DCFDFC0 (void);
// 0x00000112 UnityEngine.LogType BugsnagUnity.UnityLogMessage::get_Type()
extern void UnityLogMessage_get_Type_m65C37D6FD98A76785B019CC631EDF79BB32F5B70 (void);
// 0x00000113 System.DateTime BugsnagUnity.UnityLogMessage::get_CreatedAt()
extern void UnityLogMessage_get_CreatedAt_m7D4861060208600C566FE9516B98E20DA323646B (void);
// 0x00000114 System.Void BugsnagUnity.UnityMetadata::InitDefaultMetadata()
extern void UnityMetadata_InitDefaultMetadata_m032E205B70564926B63E9021EA3CBCEDD0979738 (void);
// 0x00000115 System.Void BugsnagUnity.UnityMetadata::InitAppMetadata()
extern void UnityMetadata_InitAppMetadata_m38FA19D680860423CFBA4F011C30611260894313 (void);
// 0x00000116 System.Void BugsnagUnity.UnityMetadata::InitDeviceMetadata()
extern void UnityMetadata_InitDeviceMetadata_m91E3A7A50353D4C043B3E70E0F26B7FAE21C0B42 (void);
// 0x00000117 System.Void BugsnagUnity.UnityMetadata::.cctor()
extern void UnityMetadata__cctor_m7B4CE8E524887FD9748127FF34382B4D05C391FF (void);
// 0x00000118 BugsnagUnity.NativeInterface BugsnagUnity.Breadcrumbs::get_NativeInterface()
extern void Breadcrumbs_get_NativeInterface_m14E86EF4ABEC5BA26F3DDCE7E677E186F6BA2BB3 (void);
// 0x00000119 System.Void BugsnagUnity.Breadcrumbs::.ctor(BugsnagUnity.NativeInterface)
extern void Breadcrumbs__ctor_mFC66C2A97F79EFCCFC8293670D8ABFAF52BB31F4 (void);
// 0x0000011A System.Void BugsnagUnity.Breadcrumbs::Leave(System.String,BugsnagUnity.Payload.BreadcrumbType,System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void Breadcrumbs_Leave_m0D8CD8A994A625D941B31AA6A8D46886A8A0E6E9 (void);
// 0x0000011B System.Void BugsnagUnity.Breadcrumbs::Leave(BugsnagUnity.Payload.Breadcrumb)
extern void Breadcrumbs_Leave_m565C3E2E86FED49F5F224BC9519620EFFACCE421 (void);
// 0x0000011C BugsnagUnity.Payload.Breadcrumb[] BugsnagUnity.Breadcrumbs::Retrieve()
extern void Breadcrumbs_Retrieve_mCB1B4EE0A6D290A85C671BD112A07550A9FC405F (void);
// 0x0000011D BugsnagUnity.IConfiguration BugsnagUnity.NativeClient::get_Configuration()
extern void NativeClient_get_Configuration_m7B67AB080D98E10EB3E21DF3D466BE4FBA2A35C4 (void);
// 0x0000011E BugsnagUnity.IBreadcrumbs BugsnagUnity.NativeClient::get_Breadcrumbs()
extern void NativeClient_get_Breadcrumbs_mA88F13C4F531B76CAED74F4CB4EF0AE01AFAEF0F (void);
// 0x0000011F BugsnagUnity.IDelivery BugsnagUnity.NativeClient::get_Delivery()
extern void NativeClient_get_Delivery_mE403E7CB048043F43379AA7585F12CE8B413EEC2 (void);
// 0x00000120 System.Void BugsnagUnity.NativeClient::.ctor(BugsnagUnity.IConfiguration)
extern void NativeClient__ctor_m0EDB27652695C65C87F9E9D1AA199C0D8DC85AAC (void);
// 0x00000121 System.Void BugsnagUnity.NativeClient::PopulateApp(BugsnagUnity.Payload.App)
extern void NativeClient_PopulateApp_m968E534FB07DAFEF285CB32A76D8A74DFDFD4AE0 (void);
// 0x00000122 System.Void BugsnagUnity.NativeClient::PopulateDevice(BugsnagUnity.Payload.Device)
extern void NativeClient_PopulateDevice_mBA0E9F982AB5CD88E677EF577DC34DC8A4D91098 (void);
// 0x00000123 System.Void BugsnagUnity.NativeClient::PopulateUser(BugsnagUnity.Payload.User)
extern void NativeClient_PopulateUser_m90703C14136F1EA708921B56339FD6C11D3ED284 (void);
// 0x00000124 System.Void BugsnagUnity.NativeClient::SetMetadata(System.String,System.Collections.Generic.Dictionary`2<System.String,System.String>)
extern void NativeClient_SetMetadata_mD0391D3C25D59ED0F727DBE83E9AA5AB98374274 (void);
// 0x00000125 System.Void BugsnagUnity.NativeClient::PopulateMetadata(BugsnagUnity.Payload.Metadata)
extern void NativeClient_PopulateMetadata_m8EE3D2777634829EF897FF5711352DF64A8CB75C (void);
// 0x00000126 System.Void BugsnagUnity.NativeClient::MergeDictionaries(System.Collections.Generic.Dictionary`2<System.String,System.Object>,System.Collections.Generic.Dictionary`2<System.String,System.Object>)
extern void NativeClient_MergeDictionaries_m42DDF6BBC2ABE6857C2A9D9988A648BA615FB772 (void);
// 0x00000127 System.Void BugsnagUnity.NativeClient::SetSession(BugsnagUnity.Payload.Session)
extern void NativeClient_SetSession_m5B18F5600E5599D7EA3CBE1FBF2FA2DBDF403E04 (void);
// 0x00000128 System.Void BugsnagUnity.NativeClient::SetUser(BugsnagUnity.Payload.User)
extern void NativeClient_SetUser_m4BC41A2DFE4421F3FCC5E42AC5BFD11CD20BAB66 (void);
// 0x00000129 System.Void BugsnagUnity.NativeClient::SetContext(System.String)
extern void NativeClient_SetContext_mDDCBFFA67E2CC6F6F9A37F4E180B09B5EDF550C4 (void);
// 0x0000012A System.Void BugsnagUnity.NativeInterface::.ctor(BugsnagUnity.IConfiguration)
extern void NativeInterface__ctor_m49D7A875A7E68FD0E3DFE6EADE440757E8A5D880 (void);
// 0x0000012B UnityEngine.AndroidJavaObject BugsnagUnity.NativeInterface::CreateNativeConfig(BugsnagUnity.IConfiguration)
extern void NativeInterface_CreateNativeConfig_mC9146C1B42D424892CF3E413D9936C9A44BFDB44 (void);
// 0x0000012C System.Void BugsnagUnity.NativeInterface::ConfigureNotifierInfo(UnityEngine.AndroidJavaObject)
extern void NativeInterface_ConfigureNotifierInfo_m84D6741E63A8BDA7B7AE4C7077EF4986BB84633A (void);
// 0x0000012D System.Boolean BugsnagUnity.NativeInterface::PushLocalFrame()
extern void NativeInterface_PushLocalFrame_mB85016D26D437AA544D033FA984A4D7BDFAECE48 (void);
// 0x0000012E System.Void BugsnagUnity.NativeInterface::PopLocalFrame()
extern void NativeInterface_PopLocalFrame_m09A8B357A96F47064EB2A9BF7E8C7CA9B3132878 (void);
// 0x0000012F System.Void BugsnagUnity.NativeInterface::SetContext(System.String)
extern void NativeInterface_SetContext_mB8B4FD5D85FE856D72B6A172A626AF88E404C2BD (void);
// 0x00000130 System.Void BugsnagUnity.NativeInterface::SetUser(BugsnagUnity.Payload.User)
extern void NativeInterface_SetUser_m780B17C7C2FD5DB2C8FB32C0941D56F1A6E206C6 (void);
// 0x00000131 System.Void BugsnagUnity.NativeInterface::SetSession(BugsnagUnity.Payload.Session)
extern void NativeInterface_SetSession_mE41D506428512530DD9D42628B2AEC57118D9547 (void);
// 0x00000132 System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::GetApp()
extern void NativeInterface_GetApp_mEBEF5C1760EDD7F9EF9A8797A3A1D292A5D78B80 (void);
// 0x00000133 System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::GetDevice()
extern void NativeInterface_GetDevice_m05CF7C915DB2F6AC4E2D6A858A976379358C6F16 (void);
// 0x00000134 System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::GetMetadata()
extern void NativeInterface_GetMetadata_mCF660982F58F7369EDC8228AD23455164120CC0C (void);
// 0x00000135 System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::GetUser()
extern void NativeInterface_GetUser_m961A8EDE1F107A93595E5A28D8FB207923CB3605 (void);
// 0x00000136 System.Void BugsnagUnity.NativeInterface::RemoveMetadata(System.String)
extern void NativeInterface_RemoveMetadata_m1B0074AF1F501694BED8B8DD18F1FD8580398025 (void);
// 0x00000137 System.Void BugsnagUnity.NativeInterface::AddToTab(System.String,System.String,System.String)
extern void NativeInterface_AddToTab_mFE4E83C4867787EE06DA4469260B3F40FA1504E4 (void);
// 0x00000138 System.Void BugsnagUnity.NativeInterface::LeaveBreadcrumb(System.String,System.String,System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void NativeInterface_LeaveBreadcrumb_mC999007B6983EA5FC27FD216D7F490AF1E3EEDC5 (void);
// 0x00000139 System.Collections.Generic.List`1<BugsnagUnity.Payload.Breadcrumb> BugsnagUnity.NativeInterface::GetBreadcrumbs()
extern void NativeInterface_GetBreadcrumbs_m425A40027D35DBF036F09771D02A216A62432ED3 (void);
// 0x0000013A System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::GetJavaMapData(System.String)
extern void NativeInterface_GetJavaMapData_m5FFAD419C6C4E97E40E3AFA785FB88AEF4FF99E1 (void);
// 0x0000013B System.Object[] BugsnagUnity.NativeInterface::ConvertStringArgsToNative(System.Object[])
extern void NativeInterface_ConvertStringArgsToNative_m810B1DCB9BF1EB1ED1BDCED46CC9DC365E377D17 (void);
// 0x0000013C System.Void BugsnagUnity.NativeInterface::ReleaseConvertedStringArgs(System.Object[],System.Object[])
extern void NativeInterface_ReleaseConvertedStringArgs_m23D17193044F778D81C948772DC00740A98BEE22 (void);
// 0x0000013D System.Void BugsnagUnity.NativeInterface::CallNativeVoidMethod(System.String,System.String,System.Object[])
extern void NativeInterface_CallNativeVoidMethod_m7BE079D1204CBA64F289545F50B1CAB4434F5981 (void);
// 0x0000013E System.IntPtr BugsnagUnity.NativeInterface::CallNativeObjectMethodRef(System.String,System.String,System.Object[])
extern void NativeInterface_CallNativeObjectMethodRef_m648989F4A1B918BA3B991A9A01DD2E5F580FD037 (void);
// 0x0000013F System.Boolean BugsnagUnity.NativeInterface::bsg_unity_isJNIAttached()
extern void NativeInterface_bsg_unity_isJNIAttached_m0A825C66EE7AB66DBF8E8EDD37CDE51F825449F5 (void);
// 0x00000140 BugsnagUnity.Payload.Breadcrumb BugsnagUnity.NativeInterface::ConvertToBreadcrumb(System.IntPtr)
extern void NativeInterface_ConvertToBreadcrumb_m79CA3711D83F7E1C4098CBE97DEFBB2C519874A8 (void);
// 0x00000141 UnityEngine.AndroidJavaObject BugsnagUnity.NativeInterface::BuildJavaMapDisposable(System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void NativeInterface_BuildJavaMapDisposable_m41C81E08629E8AAC25A8DFBDA9D43F2BD2A887BA (void);
// 0x00000142 UnityEngine.AndroidJavaObject BugsnagUnity.NativeInterface::BuildJavaStringDisposable(System.String)
extern void NativeInterface_BuildJavaStringDisposable_m341176AF5328E1E59C2C71D51C91AB3A48973BC3 (void);
// 0x00000143 System.Boolean BugsnagUnity.NativeInterface::IsUnity2019OrNewer()
extern void NativeInterface_IsUnity2019OrNewer_mF18DCC4BB85B2B2C2FFEABF29EC7ABFC4377120E (void);
// 0x00000144 System.Boolean BugsnagUnity.NativeInterface::CanRunJNI()
extern void NativeInterface_CanRunJNI_mC12DE0B2F5D8DBF5C47EB76A3204FA4D76913A78 (void);
// 0x00000145 System.Collections.Generic.Dictionary`2<System.String,System.Object> BugsnagUnity.NativeInterface::DictionaryFromJavaMap(System.IntPtr)
extern void NativeInterface_DictionaryFromJavaMap_m995993546F0E94C8B6F812C7A40FF014B02336CE (void);
// 0x00000146 System.Type BugsnagUnity.Reflection.ReflectionUtils::GetTypeInfo(System.Type)
extern void ReflectionUtils_GetTypeInfo_m76CE7EB54A118E3FE726DB364C81EB90874AA39E (void);
// 0x00000147 System.Boolean BugsnagUnity.Reflection.ReflectionUtils::IsValueType(System.Type)
extern void ReflectionUtils_IsValueType_m2EFDC263358CF0BCC693DD1EB929AB665A80F1E9 (void);
// 0x00000148 System.Collections.Generic.IEnumerable`1<System.Reflection.ConstructorInfo> BugsnagUnity.Reflection.ReflectionUtils::GetConstructors(System.Type)
extern void ReflectionUtils_GetConstructors_m083A55CB9BAF61C0CB26A700D122F15AC2061677 (void);
// 0x00000149 System.Reflection.ConstructorInfo BugsnagUnity.Reflection.ReflectionUtils::GetConstructorInfo(System.Type,System.Type[])
extern void ReflectionUtils_GetConstructorInfo_m20A885BDE5699F098BA4B6F30296E617E69A85CA (void);
// 0x0000014A System.Collections.Generic.IEnumerable`1<System.Reflection.PropertyInfo> BugsnagUnity.Reflection.ReflectionUtils::GetProperties(System.Type)
extern void ReflectionUtils_GetProperties_mE05CCFCE775C04BC28296C4C5DE681AA8C41340D (void);
// 0x0000014B System.Collections.Generic.IEnumerable`1<System.Reflection.FieldInfo> BugsnagUnity.Reflection.ReflectionUtils::GetFields(System.Type)
extern void ReflectionUtils_GetFields_mFFCD16CCDF3EAF3E62003F1401A010FA6C13D63D (void);
// 0x0000014C System.Reflection.MethodInfo BugsnagUnity.Reflection.ReflectionUtils::GetGetterMethodInfo(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetGetterMethodInfo_m7673CD82B21B61DD47B701A1776523D4C009E88C (void);
// 0x0000014D System.Reflection.MethodInfo BugsnagUnity.Reflection.ReflectionUtils::GetSetterMethodInfo(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetSetterMethodInfo_m6318D907AB552B3CAF51EB8AF6206FBA8842D1AA (void);
// 0x0000014E BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate BugsnagUnity.Reflection.ReflectionUtils::GetContructor(System.Type,System.Type[])
extern void ReflectionUtils_GetContructor_mB0ECD6B627A84F3036A10E7A72428408117188E2 (void);
// 0x0000014F BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate BugsnagUnity.Reflection.ReflectionUtils::GetConstructorByExpression(System.Reflection.ConstructorInfo)
extern void ReflectionUtils_GetConstructorByExpression_mB06D847143EB25B5735B2E79308AB027E527034C (void);
// 0x00000150 BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate BugsnagUnity.Reflection.ReflectionUtils::GetConstructorByExpression(System.Type,System.Type[])
extern void ReflectionUtils_GetConstructorByExpression_m5FD42036FF56A1028F243E73F3C51D92F385A612 (void);
// 0x00000151 BugsnagUnity.Reflection.ReflectionUtils/GetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetGetMethod(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetGetMethod_mC3AF560399337731C6F715795014FFEA6DF186FD (void);
// 0x00000152 BugsnagUnity.Reflection.ReflectionUtils/GetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetGetMethod(System.Reflection.FieldInfo)
extern void ReflectionUtils_GetGetMethod_mC897E931904F576E11B7578250C2B47E7648E3BA (void);
// 0x00000153 BugsnagUnity.Reflection.ReflectionUtils/GetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetGetMethodByExpression(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetGetMethodByExpression_m2C27434FAB45F2576098F0966F587E3A4BB56A3B (void);
// 0x00000154 BugsnagUnity.Reflection.ReflectionUtils/GetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetGetMethodByExpression(System.Reflection.FieldInfo)
extern void ReflectionUtils_GetGetMethodByExpression_m80CB7DCF97A728203A967800C949BE3B9F5204C6 (void);
// 0x00000155 BugsnagUnity.Reflection.ReflectionUtils/SetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetSetMethod(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetSetMethod_m22297555552DE04979E9B8A550A0B19A7BCFA107 (void);
// 0x00000156 BugsnagUnity.Reflection.ReflectionUtils/SetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetSetMethod(System.Reflection.FieldInfo)
extern void ReflectionUtils_GetSetMethod_m855412286F8FA211FDF9101CDC7B2B83A0650A4A (void);
// 0x00000157 BugsnagUnity.Reflection.ReflectionUtils/SetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetSetMethodByExpression(System.Reflection.PropertyInfo)
extern void ReflectionUtils_GetSetMethodByExpression_mE2C7AD016142BCDF20FAAF81C0918A41DA0C13D6 (void);
// 0x00000158 BugsnagUnity.Reflection.ReflectionUtils/SetDelegate BugsnagUnity.Reflection.ReflectionUtils::GetSetMethodByExpression(System.Reflection.FieldInfo)
extern void ReflectionUtils_GetSetMethodByExpression_m6F422BAF2A66DDCD1F39A32B9EEB1DFB414341E9 (void);
// 0x00000159 System.Linq.Expressions.BinaryExpression BugsnagUnity.Reflection.ReflectionUtils::Assign(System.Linq.Expressions.Expression,System.Linq.Expressions.Expression)
extern void ReflectionUtils_Assign_m83081BE19A26CA7D1E02F49EC45072056ADCF7CC (void);
// 0x0000015A System.Void BugsnagUnity.Reflection.ReflectionUtils::.cctor()
extern void ReflectionUtils__cctor_mF3B5CD75BB2FEC95CE35BA7823D378A99787E293 (void);
// 0x0000015B System.Void BugsnagUnity.Reflection.ReflectionUtils/GetDelegate::.ctor(System.Object,System.IntPtr)
extern void GetDelegate__ctor_mA7FBD3844BCEB926506C50FAB4251FE1581F7F1E (void);
// 0x0000015C System.Object BugsnagUnity.Reflection.ReflectionUtils/GetDelegate::Invoke(System.Object)
extern void GetDelegate_Invoke_mC8AB5516BAA6E4AE36B7DECB66A45599A009DBF5 (void);
// 0x0000015D System.Void BugsnagUnity.Reflection.ReflectionUtils/SetDelegate::.ctor(System.Object,System.IntPtr)
extern void SetDelegate__ctor_mDBA506C897F81D05877D6F8E2810C652F81AB91E (void);
// 0x0000015E System.Void BugsnagUnity.Reflection.ReflectionUtils/SetDelegate::Invoke(System.Object,System.Object)
extern void SetDelegate_Invoke_mAD5E00776C46ECBBEE83A9892031AD475E168D4E (void);
// 0x0000015F System.Void BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate::.ctor(System.Object,System.IntPtr)
extern void ConstructorDelegate__ctor_mBED41D9B1F7BF383D9C255701B611AF3C32C3372 (void);
// 0x00000160 System.Object BugsnagUnity.Reflection.ReflectionUtils/ConstructorDelegate::Invoke(System.Object[])
extern void ConstructorDelegate_Invoke_m93DFB0E4B38CBA68CF262EF01AE72791F701FC10 (void);
// 0x00000161 System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionaryValueFactory`2::.ctor(System.Object,System.IntPtr)
// 0x00000162 TValue BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionaryValueFactory`2::Invoke(TKey)
// 0x00000163 T BugsnagUnity.Reflection.ReflectionUtils/Assigner`1::Assign(T&,T)
// 0x00000164 System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::.ctor(BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionaryValueFactory`2<TKey,TValue>)
// 0x00000165 TValue BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Get(TKey)
// 0x00000166 TValue BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::AddValue(TKey)
// 0x00000167 System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Add(TKey,TValue)
// 0x00000168 System.Boolean BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::ContainsKey(TKey)
// 0x00000169 System.Collections.Generic.ICollection`1<TKey> BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::get_Keys()
// 0x0000016A System.Boolean BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Remove(TKey)
// 0x0000016B System.Collections.Generic.ICollection`1<TValue> BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::get_Values()
// 0x0000016C TValue BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::get_Item(TKey)
// 0x0000016D System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::set_Item(TKey,TValue)
// 0x0000016E System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Add(System.Collections.Generic.KeyValuePair`2<TKey,TValue>)
// 0x0000016F System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Clear()
// 0x00000170 System.Boolean BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Contains(System.Collections.Generic.KeyValuePair`2<TKey,TValue>)
// 0x00000171 System.Void BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::CopyTo(System.Collections.Generic.KeyValuePair`2<TKey,TValue>[],System.Int32)
// 0x00000172 System.Int32 BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::get_Count()
// 0x00000173 System.Boolean BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::get_IsReadOnly()
// 0x00000174 System.Boolean BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::Remove(System.Collections.Generic.KeyValuePair`2<TKey,TValue>)
// 0x00000175 System.Collections.Generic.IEnumerator`1<System.Collections.Generic.KeyValuePair`2<TKey,TValue>> BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::GetEnumerator()
// 0x00000176 System.Collections.IEnumerator BugsnagUnity.Reflection.ReflectionUtils/ThreadSafeDictionary`2::System.Collections.IEnumerable.GetEnumerator()
// 0x00000177 System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass27_0::.ctor()
extern void U3CU3Ec__DisplayClass27_0__ctor_m9ED512FA1ACEC5EDA49B1285BDACAA349BC6E63A (void);
// 0x00000178 System.Object BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass27_0::<GetConstructorByExpression>b__0(System.Object[])
extern void U3CU3Ec__DisplayClass27_0_U3CGetConstructorByExpressionU3Eb__0_m9C0447B805A8F17B003F31BF6BC5B38AB94AB662 (void);
// 0x00000179 System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass33_0::.ctor()
extern void U3CU3Ec__DisplayClass33_0__ctor_mB7EBCE104FDCB78CB491A50B0D737314B5E3E08F (void);
// 0x0000017A System.Object BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass33_0::<GetGetMethodByExpression>b__0(System.Object)
extern void U3CU3Ec__DisplayClass33_0_U3CGetGetMethodByExpressionU3Eb__0_m77A1BA4BDB9A46F473B2286AB2A131E58EE26C87 (void);
// 0x0000017B System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass34_0::.ctor()
extern void U3CU3Ec__DisplayClass34_0__ctor_mCB3D4CFB698F162A1334B99A388AC2508C290F7B (void);
// 0x0000017C System.Object BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass34_0::<GetGetMethodByExpression>b__0(System.Object)
extern void U3CU3Ec__DisplayClass34_0_U3CGetGetMethodByExpressionU3Eb__0_m4C89BBBAEB6318CAA2CB8AEC7AADA98057563A1A (void);
// 0x0000017D System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass39_0::.ctor()
extern void U3CU3Ec__DisplayClass39_0__ctor_m9CCE1032B8D57EA3D364275DA5CBE0E17A90C0A8 (void);
// 0x0000017E System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass39_0::<GetSetMethodByExpression>b__0(System.Object,System.Object)
extern void U3CU3Ec__DisplayClass39_0_U3CGetSetMethodByExpressionU3Eb__0_m6D1332BA68455980426DC8099BD3176F5DE6D8A4 (void);
// 0x0000017F System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass40_0::.ctor()
extern void U3CU3Ec__DisplayClass40_0__ctor_m1342687ACA4B2ADD1320D68860D74010B9B85E8D (void);
// 0x00000180 System.Void BugsnagUnity.Reflection.ReflectionUtils/<>c__DisplayClass40_0::<GetSetMethodByExpression>b__0(System.Object,System.Object)
extern void U3CU3Ec__DisplayClass40_0_U3CGetSetMethodByExpressionU3Eb__0_mECB5D755E37E618F77C7F58A82DA2CB506686101 (void);
// 0x00000181 System.Boolean BugsnagUnity.Polyfills.String::IsNullOrWhiteSpace(System.String)
extern void String_IsNullOrWhiteSpace_mC172FD901004D8C0F372C212929C4D061095342F (void);
// 0x00000182 System.Void BugsnagUnity.Payload.App::.ctor(BugsnagUnity.IConfiguration)
extern void App__ctor_m05F5B88D6BCA2B390DCB0ACE93B4DEF1C2045203 (void);
// 0x00000183 System.Void BugsnagUnity.Payload.App::set_Version(System.String)
extern void App_set_Version_mAEAAEEA79540CC7E878914514A9B2C7AA18BBD0C (void);
// 0x00000184 System.Void BugsnagUnity.Payload.App::set_ReleaseStage(System.String)
extern void App_set_ReleaseStage_m089DF4E99CD95574F4BB90B8B571C84BD8B62674 (void);
// 0x00000185 System.Void BugsnagUnity.Payload.App::set_InForeground(System.Boolean)
extern void App_set_InForeground_mCADCF6CED9059D8ADE750E94B68539DA96698A69 (void);
// 0x00000186 System.Void BugsnagUnity.Payload.App::set_DurationInForeground(System.TimeSpan)
extern void App_set_DurationInForeground_m89BA978D8E56AD8E2AFA94EE9D6128DBF785084B (void);
// 0x00000187 BugsnagUnity.Payload.Breadcrumb BugsnagUnity.Payload.Breadcrumb::FromReport(BugsnagUnity.Payload.Report)
extern void Breadcrumb_FromReport_m1F5BF1ABC1AFBFD1F174F6D845827C230234864D (void);
// 0x00000188 System.Void BugsnagUnity.Payload.Breadcrumb::.ctor(System.String,System.String,System.String,System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void Breadcrumb__ctor_m857F339E0113D68323D198A1473F588F4B66A9FD (void);
// 0x00000189 System.Void BugsnagUnity.Payload.Breadcrumb::.ctor(System.String,BugsnagUnity.Payload.BreadcrumbType,System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void Breadcrumb__ctor_mEB8657A8298A18A52324AF34D0D483174800F011 (void);
// 0x0000018A System.String BugsnagUnity.Payload.Breadcrumb::get_Name()
extern void Breadcrumb_get_Name_m2BFB55C0F4C80549EF4BA43D3D8B5B68BFF134DF (void);
// 0x0000018B System.String BugsnagUnity.Payload.Breadcrumb::get_Type()
extern void Breadcrumb_get_Type_m2E64022FFF88EA7C848B47582342E5795E0D2AEE (void);
// 0x0000018C System.Collections.Generic.IDictionary`2<System.String,System.String> BugsnagUnity.Payload.Breadcrumb::get_Metadata()
extern void Breadcrumb_get_Metadata_m7AB19B3E9A424D622A606401523F39DA7E863142 (void);
// 0x0000018D System.Void BugsnagUnity.Payload.Device::InitUnityVersion()
extern void Device_InitUnityVersion_m64F4C20FFB719B3E3A5F48DB00F4D25EB4C4803D (void);
// 0x0000018E System.Void BugsnagUnity.Payload.Device::.ctor()
extern void Device__ctor_mFC6FAD7C418C5BADEC0E5B42F3F0D1E7B9C62411 (void);
// 0x0000018F System.Void BugsnagUnity.Payload.Device::.ctor(System.String)
extern void Device__ctor_mB31CE70F695910C31457B019244E4652B05A9672 (void);
// 0x00000190 System.Void BugsnagUnity.Payload.Device::AddRuntimeVersions(BugsnagUnity.IConfiguration)
extern void Device_AddRuntimeVersions_m39F755547A2CCB49E286DF90BE82AAF7AB487751 (void);
// 0x00000191 System.String BugsnagUnity.Payload.Device::get_Hostname()
extern void Device_get_Hostname_m7F995122AC544B8D09440214F625D964D8291A19 (void);
// 0x00000192 System.String BugsnagUnity.Payload.Device::get_OsName()
extern void Device_get_OsName_m4E47A2702D50ECF537B2822A190415D6C09D3DB6 (void);
// 0x00000193 System.Void BugsnagUnity.Payload.Event::.ctor(System.String,BugsnagUnity.Payload.Metadata,BugsnagUnity.Payload.App,BugsnagUnity.Payload.Device,BugsnagUnity.Payload.User,BugsnagUnity.Payload.Exception[],BugsnagUnity.Payload.HandledState,BugsnagUnity.Payload.Breadcrumb[],BugsnagUnity.Payload.Session,System.Nullable`1<UnityEngine.LogType>)
extern void Event__ctor_mEBFC7B6B89B61C6986879B02CFE9F797F086D64B (void);
// 0x00000194 BugsnagUnity.Payload.Metadata BugsnagUnity.Payload.Event::get_Metadata()
extern void Event_get_Metadata_m9A99A62619DD1ECF7831A084D087232BDB614D37 (void);
// 0x00000195 System.Boolean BugsnagUnity.Payload.Event::get_IsHandled()
extern void Event_get_IsHandled_m35E3B31E4E2EF423A7C41F32438D518CC8F8F41C (void);
// 0x00000196 System.String BugsnagUnity.Payload.Event::get_Context()
extern void Event_get_Context_mF2EFDEEE22C1F57EAD793E996272D08888F75460 (void);
// 0x00000197 BugsnagUnity.Payload.Exception[] BugsnagUnity.Payload.Event::get_Exceptions()
extern void Event_get_Exceptions_mE65361063C29C44FFE06A432291B2B5394AD337B (void);
// 0x00000198 System.Void BugsnagUnity.Payload.Event::set_HandledState(BugsnagUnity.Payload.HandledState)
extern void Event_set_HandledState_m9C8BE0A1D7CB9AA7AB26959C7686A0D0F587B328 (void);
// 0x00000199 BugsnagUnity.Payload.HandledState BugsnagUnity.Payload.Exception::get_HandledState()
extern void Exception_get_HandledState_m14334619113E50A17993AEF6ABB897633CC0F4B3 (void);
// 0x0000019A System.Void BugsnagUnity.Payload.Exception::.ctor(System.String,System.String,BugsnagUnity.Payload.StackTraceLine[],BugsnagUnity.Payload.HandledState)
extern void Exception__ctor_mA062DE928989F4832F1024D802C62CBF7B2EDB77 (void);
// 0x0000019B System.String BugsnagUnity.Payload.Exception::get_ErrorClass()
extern void Exception_get_ErrorClass_mEC075C48F95B9263F0A560086BE93BAABA20C44F (void);
// 0x0000019C System.String BugsnagUnity.Payload.Exception::get_ErrorMessage()
extern void Exception_get_ErrorMessage_m993AF3F983F8CE7AD0FFBA00FE516743700D408F (void);
// 0x0000019D BugsnagUnity.Payload.Exception BugsnagUnity.Payload.Exception::FromUnityLogMessage(BugsnagUnity.UnityLogMessage,System.Diagnostics.StackFrame[],BugsnagUnity.Severity,System.Boolean)
extern void Exception_FromUnityLogMessage_m27AD01566A06B28E6A7A48EE4607B5DFB4DBB4C3 (void);
// 0x0000019E System.Boolean BugsnagUnity.Payload.Exception::ShouldSend(BugsnagUnity.UnityLogMessage)
extern void Exception_ShouldSend_m01BD69F77961C044F8091854259032642CC81716 (void);
// 0x0000019F System.Void BugsnagUnity.Payload.Exception::.cctor()
extern void Exception__cctor_m9BC419DC4816C4E38EB7EB89DD7CC231B87938C3 (void);
// 0x000001A0 BugsnagUnity.Payload.HandledState BugsnagUnity.Payload.HandledState::ForUnhandledException()
extern void HandledState_ForUnhandledException_m6790D7D905DB0A97F7F975A53AF1701D6D1FC9C4 (void);
// 0x000001A1 BugsnagUnity.Payload.HandledState BugsnagUnity.Payload.HandledState::ForUnityLogMessage(BugsnagUnity.Severity)
extern void HandledState_ForUnityLogMessage_m7837D6EE958C30FC2F340FBD5594ABBF3307C7B2 (void);
// 0x000001A2 System.Void BugsnagUnity.Payload.HandledState::.ctor(System.Boolean,BugsnagUnity.Severity,BugsnagUnity.Payload.HandledState/SeverityReason)
extern void HandledState__ctor_m785F272C2202C8711E478D4DF50024DDA5E9CEFB (void);
// 0x000001A3 System.Boolean BugsnagUnity.Payload.HandledState::get_Handled()
extern void HandledState_get_Handled_m3B353FFF6FE103AD676E629ED61A31FC7C408C90 (void);
// 0x000001A4 BugsnagUnity.Payload.HandledState/SeverityReason BugsnagUnity.Payload.HandledState/SeverityReason::ForUnhandledException()
extern void SeverityReason_ForUnhandledException_mAE0F11A291E9B71F32D11E554081A9757DD2A18C (void);
// 0x000001A5 BugsnagUnity.Payload.HandledState/SeverityReason BugsnagUnity.Payload.HandledState/SeverityReason::ForHandledException()
extern void SeverityReason_ForHandledException_m50CB622ECDCCA01D25CCBBEDC377C1DDB5F8A13E (void);
// 0x000001A6 System.Void BugsnagUnity.Payload.HandledState/SeverityReason::.ctor(System.String,System.Collections.Generic.IDictionary`2<System.String,System.String>)
extern void SeverityReason__ctor_m8AA3CD14300711196A769D62A802DA7EEDBA0B33 (void);
// 0x000001A7 System.Uri BugsnagUnity.Payload.IPayload::get_Endpoint()
// 0x000001A8 System.Collections.Generic.KeyValuePair`2<System.String,System.String>[] BugsnagUnity.Payload.IPayload::get_Headers()
// 0x000001A9 System.Void BugsnagUnity.Payload.Metadata::.ctor()
extern void Metadata__ctor_mF4D68CBC6B97FC64C61D86F6479CD3FF18DD293D (void);
// 0x000001AA System.Void BugsnagUnity.Payload.Metadata::.ctor(BugsnagUnity.INativeClient)
extern void Metadata__ctor_m4F5F4F12F06CBEB2429A3E135E0312F6ABC81D40 (void);
// 0x000001AB System.Void BugsnagUnity.Payload.Method::.ctor(System.Reflection.MethodBase)
extern void Method__ctor_m0EB6B66BBFDA262CE06EA155AF8D233F7D1B1A12 (void);
// 0x000001AC System.String BugsnagUnity.Payload.Method::DisplayName()
extern void Method_DisplayName_m29B9BCC938AC7B51F280BADF85AF60F87D12271E (void);
// 0x000001AD System.Void BugsnagUnity.Payload.Method/<>c::.cctor()
extern void U3CU3Ec__cctor_m61D88ADEF2C1283F2DA74B6AA18C20AC85667841 (void);
// 0x000001AE System.Void BugsnagUnity.Payload.Method/<>c::.ctor()
extern void U3CU3Ec__ctor_mC4046C2673B89C6E147A7C0F6113A7D7AB208D74 (void);
// 0x000001AF System.String BugsnagUnity.Payload.Method/<>c::<DisplayName>b__2_1(System.Type)
extern void U3CU3Ec_U3CDisplayNameU3Eb__2_1_m65AD70F846DD2B050B6430488F7DB5EF1FFB124F (void);
// 0x000001B0 System.String BugsnagUnity.Payload.Method/<>c::<DisplayName>b__2_0(System.Reflection.ParameterInfo)
extern void U3CU3Ec_U3CDisplayNameU3Eb__2_0_mBF5EDB23CEC3E0D06718D4A0D351489AB35AC15D (void);
// 0x000001B1 System.Void BugsnagUnity.Payload.MethodParameter::.ctor(System.Reflection.ParameterInfo)
extern void MethodParameter__ctor_mD5965B64AECD0E08FD27382DD12C9384224D1114 (void);
// 0x000001B2 System.String BugsnagUnity.Payload.MethodParameter::DisplayName()
extern void MethodParameter_DisplayName_m149CA20BDC55C9B9B65884E1D998980C356D2698 (void);
// 0x000001B3 BugsnagUnity.Payload.NotifierInfo BugsnagUnity.Payload.NotifierInfo::get_Instance()
extern void NotifierInfo_get_Instance_m9E36CCDF62776C1233273D17F2715747FBA408FE (void);
// 0x000001B4 System.Void BugsnagUnity.Payload.NotifierInfo::.ctor()
extern void NotifierInfo__ctor_m941F4574693D324A8C9CE4BD179194D8F3791F0B (void);
// 0x000001B5 System.Void BugsnagUnity.Payload.NotifierInfo::.cctor()
extern void NotifierInfo__cctor_m70812A17E3F1636B8362D22D19B75C25BB75B5D9 (void);
// 0x000001B6 System.Void BugsnagUnity.Payload.PayloadExtensions::AddToPayload(System.Collections.Generic.IDictionary`2<System.String,T>,System.String,T)
// 0x000001B7 U BugsnagUnity.Payload.PayloadExtensions::Get(System.Collections.Generic.Dictionary`2<T,U>,T)
// 0x000001B8 BugsnagUnity.IConfiguration BugsnagUnity.Payload.Report::get_Configuration()
extern void Report_get_Configuration_m6B75DC8894AB7A3750D72F7E0979E7EB454001CF (void);
// 0x000001B9 System.Uri BugsnagUnity.Payload.Report::get_Endpoint()
extern void Report_get_Endpoint_m1350AF0815A2FA8C46E2F62F8A3E99E28B02D363 (void);
// 0x000001BA System.Collections.Generic.KeyValuePair`2<System.String,System.String>[] BugsnagUnity.Payload.Report::get_Headers()
extern void Report_get_Headers_mC50A503F8A98CCA4105803F5A29460E23DD9E89B (void);
// 0x000001BB System.Boolean BugsnagUnity.Payload.Report::get_Ignored()
extern void Report_get_Ignored_mA6144148B345C13E6BACFB2BB86D89125C73933E (void);
// 0x000001BC System.Void BugsnagUnity.Payload.Report::set_Ignored(System.Boolean)
extern void Report_set_Ignored_m4DFEB6B9CFD98B28C6E5EE353DD1151EA9F1B1B7 (void);
// 0x000001BD System.Void BugsnagUnity.Payload.Report::.ctor(BugsnagUnity.IConfiguration,BugsnagUnity.Payload.Event)
extern void Report__ctor_m512FD47A9A4D43F3AD37B13A4DEB9739A6C4D71F (void);
// 0x000001BE BugsnagUnity.Payload.Event BugsnagUnity.Payload.Report::get_Event()
extern void Report_get_Event_mF229911E30E61DDF9A9DCA7E76A43D17BCF9F545 (void);
// 0x000001BF System.Boolean BugsnagUnity.Payload.Report::get_IsHandled()
extern void Report_get_IsHandled_mDD3B76C3145CA8310E4C05AA87EAFB9F073E4E4C (void);
// 0x000001C0 System.String BugsnagUnity.Payload.Report::get_Context()
extern void Report_get_Context_m9DB8ECF7CF6896E1644DCE01BC0D2D319B00F9CB (void);
// 0x000001C1 BugsnagUnity.Payload.Exception[] BugsnagUnity.Payload.Report::get_Exceptions()
extern void Report_get_Exceptions_m49EC7204681F47EA1EDAE6A57DA1BF41F7DF147F (void);
// 0x000001C2 System.Guid BugsnagUnity.Payload.Session::get_Id()
extern void Session_get_Id_m9CA25A2B0A621518A6402EEFFA2505D21C4313E1 (void);
// 0x000001C3 System.DateTime BugsnagUnity.Payload.Session::get_StartedAt()
extern void Session_get_StartedAt_m2F9B6BCFDC8BC04BA4A5BCD0884CFD240FBDAC9B (void);
// 0x000001C4 System.Int32 BugsnagUnity.Payload.Session::HandledCount()
extern void Session_HandledCount_m1D9835FEC2320E32DCCB97D4A90A2823E43A823C (void);
// 0x000001C5 System.Int32 BugsnagUnity.Payload.Session::UnhandledCount()
extern void Session_UnhandledCount_mD60A7A0CEAC55E78384C162444B35C743817FCE7 (void);
// 0x000001C6 BugsnagUnity.Payload.SessionEvents BugsnagUnity.Payload.Session::get_Events()
extern void Session_get_Events_m0E3FE99AE1A4467B72F5589708AD939CA58D1C0C (void);
// 0x000001C7 System.Boolean BugsnagUnity.Payload.Session::get_Stopped()
extern void Session_get_Stopped_m299B3B54036C435590D941F7A02BF90AE6DB8EF1 (void);
// 0x000001C8 System.Void BugsnagUnity.Payload.Session::.ctor()
extern void Session__ctor_mFE2011D28D4649DBA0017FC3C78A911316962277 (void);
// 0x000001C9 System.Void BugsnagUnity.Payload.Session::.ctor(System.DateTime,System.Int32,System.Int32)
extern void Session__ctor_m68E35AF8D8D1FC62B2C89B3057DEB4E29C45AC22 (void);
// 0x000001CA System.Void BugsnagUnity.Payload.Session::AddException(BugsnagUnity.Payload.Report)
extern void Session_AddException_m5130136710F2043413C6B1FC3E4E6AB5BD9C5B54 (void);
// 0x000001CB BugsnagUnity.Payload.Session BugsnagUnity.Payload.Session::Copy()
extern void Session_Copy_m43DA6299C8071371CB20799BD0FB88E0B0719F95 (void);
// 0x000001CC System.Void BugsnagUnity.Payload.SessionEvents::.ctor(System.Int32,System.Int32)
extern void SessionEvents__ctor_mB0CACD328B0433BDB49DF4D9A1BDBBF764EE58CD (void);
// 0x000001CD System.Int32 BugsnagUnity.Payload.SessionEvents::get_Handled()
extern void SessionEvents_get_Handled_m3FEE3202381E8F712F63695978C3B2C9B2AAE9C5 (void);
// 0x000001CE System.Int32 BugsnagUnity.Payload.SessionEvents::get_Unhandled()
extern void SessionEvents_get_Unhandled_m9F5D16CF84F8468DF636FBFF5B40416D1F8E6A41 (void);
// 0x000001CF System.Void BugsnagUnity.Payload.SessionEvents::IncrementHandledCount()
extern void SessionEvents_IncrementHandledCount_m3BB460E38D2A6E8E4356FA41E100FD18FB60103E (void);
// 0x000001D0 System.Void BugsnagUnity.Payload.SessionEvents::IncrementUnhandledCount()
extern void SessionEvents_IncrementUnhandledCount_m3A9A71A762D7D6028B3852D93233697ADC306DD0 (void);
// 0x000001D1 BugsnagUnity.IConfiguration BugsnagUnity.Payload.SessionReport::get_Configuration()
extern void SessionReport_get_Configuration_m503B70A91414EFCD90B51A510449927468F5C96D (void);
// 0x000001D2 System.Uri BugsnagUnity.Payload.SessionReport::get_Endpoint()
extern void SessionReport_get_Endpoint_m3706C4F3CE4D08EFDD0F62A0DE371AD6A4E68B14 (void);
// 0x000001D3 System.Collections.Generic.KeyValuePair`2<System.String,System.String>[] BugsnagUnity.Payload.SessionReport::get_Headers()
extern void SessionReport_get_Headers_mB9E24EEAE379D278E8B1B0A5A4B9501C0829C5FD (void);
// 0x000001D4 System.Void BugsnagUnity.Payload.SessionReport::.ctor(BugsnagUnity.IConfiguration,BugsnagUnity.Payload.App,BugsnagUnity.Payload.Device,BugsnagUnity.Payload.User,BugsnagUnity.Payload.Session)
extern void SessionReport__ctor_mC828FFBF592CCB63982DD86D45A3A64FE8C5337B (void);
// 0x000001D5 System.Void BugsnagUnity.Payload.SessionReport/Session::.ctor(BugsnagUnity.Payload.User,BugsnagUnity.Payload.Session)
extern void Session__ctor_m568E0F33FBDE6CC46B520C2F35AD28DE33786234 (void);
// 0x000001D6 BugsnagUnity.Payload.StackTraceLine[] BugsnagUnity.Payload.StackTrace::get_StackTraceLines()
extern void StackTrace_get_StackTraceLines_mC9E0984F4B0053561181F8E4E0244D6BCBD1F93B (void);
// 0x000001D7 System.Void BugsnagUnity.Payload.StackTrace::.ctor(System.Diagnostics.StackFrame[])
extern void StackTrace__ctor_m9B06A5E7E121F23E200021D98C934FFC4EB51530 (void);
// 0x000001D8 System.Void BugsnagUnity.Payload.StackTrace::.ctor(System.String)
extern void StackTrace__ctor_m65C5617D92937BB143249F55E306DB6D6BE3DE11 (void);
// 0x000001D9 System.Void BugsnagUnity.Payload.StackTrace::.ctor(System.String,BugsnagUnity.Payload.StackTraceFormat)
extern void StackTrace__ctor_mE9CCD3E8523EC699E1F65AA798A37F169CFEB27D (void);
// 0x000001DA System.Collections.Generic.IEnumerator`1<BugsnagUnity.Payload.StackTraceLine> BugsnagUnity.Payload.StackTrace::GetEnumerator()
extern void StackTrace_GetEnumerator_m4DA074B85E788D1D0BBFAA84103E06D3762AD8CD (void);
// 0x000001DB System.Collections.IEnumerator BugsnagUnity.Payload.StackTrace::System.Collections.IEnumerable.GetEnumerator()
extern void StackTrace_System_Collections_IEnumerable_GetEnumerator_m96E6FDB477C7AC99C8F58977101DAE8C001D6AB2 (void);
// 0x000001DC System.Void BugsnagUnity.Payload.StackTrace/<GetEnumerator>d__6::.ctor(System.Int32)
extern void U3CGetEnumeratorU3Ed__6__ctor_m32D557775744F7F30BBD17BECA5F026AD72280F2 (void);
// 0x000001DD System.Void BugsnagUnity.Payload.StackTrace/<GetEnumerator>d__6::System.IDisposable.Dispose()
extern void U3CGetEnumeratorU3Ed__6_System_IDisposable_Dispose_m3B0572963C30560609A024B021855108FC6FACC7 (void);
// 0x000001DE System.Boolean BugsnagUnity.Payload.StackTrace/<GetEnumerator>d__6::MoveNext()
extern void U3CGetEnumeratorU3Ed__6_MoveNext_m41DAC7A7666993BD6E41463D85853BCE515462F3 (void);
// 0x000001DF BugsnagUnity.Payload.StackTraceLine BugsnagUnity.Payload.StackTrace/<GetEnumerator>d__6::System.Collections.Generic.IEnumerator<BugsnagUnity.Payload.StackTraceLine>.get_Current()
extern void U3CGetEnumeratorU3Ed__6_System_Collections_Generic_IEnumeratorU3CBugsnagUnity_Payload_StackTraceLineU3E_get_Current_mDD4867E948964D769EEF06800DDB46A6BFDE5E1B (void);
// 0x000001E0 System.Object BugsnagUnity.Payload.StackTrace/<GetEnumerator>d__6::System.Collections.IEnumerator.get_Current()
extern void U3CGetEnumeratorU3Ed__6_System_Collections_IEnumerator_get_Current_mC8BAA0A2479397B9177BFDAB052533906BAAC6F8 (void);
// 0x000001E1 System.Text.RegularExpressions.Regex BugsnagUnity.Payload.StackTraceLine::get_StackTraceLineRegex()
extern void StackTraceLine_get_StackTraceLineRegex_mD2F1CC5ACECCBA418AC4729CB9935D60AB0865B4 (void);
// 0x000001E2 System.Text.RegularExpressions.Regex BugsnagUnity.Payload.StackTraceLine::get_StackTraceAndroidJavaLineRegex()
extern void StackTraceLine_get_StackTraceAndroidJavaLineRegex_m6039AF2D8DFF743BBFBA39DA84A9D172819A5B02 (void);
// 0x000001E3 BugsnagUnity.Payload.StackTraceLine BugsnagUnity.Payload.StackTraceLine::FromLogMessage(System.String)
extern void StackTraceLine_FromLogMessage_m1719C51CC4D9B915838D4D6EA5F14944F0F1F9F7 (void);
// 0x000001E4 BugsnagUnity.Payload.StackTraceLine BugsnagUnity.Payload.StackTraceLine::FromAndroidJavaMessage(System.String)
extern void StackTraceLine_FromAndroidJavaMessage_m7A0899EFCAC52A9A2BA6174DC74F13E7CFF46865 (void);
// 0x000001E5 BugsnagUnity.Payload.StackTraceLine BugsnagUnity.Payload.StackTraceLine::FromStackFrame(System.Diagnostics.StackFrame)
extern void StackTraceLine_FromStackFrame_m891FF0BC1BA5DA3C546932D6E7315A4D3985BCBA (void);
// 0x000001E6 System.Void BugsnagUnity.Payload.StackTraceLine::.ctor(System.String,System.Nullable`1<System.Int32>,System.String)
extern void StackTraceLine__ctor_mC1A1ACF90106E07CC1F78E9C814BE3541D4BF6B7 (void);
// 0x000001E7 System.Void BugsnagUnity.Payload.StackTraceLine::.cctor()
extern void StackTraceLine__cctor_m4DFC40103A010D154F8F32281D0933779A7E3E67 (void);
// 0x000001E8 System.Void BugsnagUnity.Payload.User::add_PropertyChanged(System.ComponentModel.PropertyChangedEventHandler)
extern void User_add_PropertyChanged_m635DCF6846D2A662BFF8059596339E9DFE05DD88 (void);
// 0x000001E9 System.Void BugsnagUnity.Payload.User::remove_PropertyChanged(System.ComponentModel.PropertyChangedEventHandler)
extern void User_remove_PropertyChanged_mC13C19DEE2C57B5B8B54DE039EA607D1FEB2BA5A (void);
// 0x000001EA System.Void BugsnagUnity.Payload.User::.ctor()
extern void User__ctor_mD4A56113C5A9468726C5A253C2F9FA25DFE1A89F (void);
// 0x000001EB System.String BugsnagUnity.Payload.User::get_Id()
extern void User_get_Id_mC5D896375444EE67AD9152733F2914619754E597 (void);
// 0x000001EC System.Void BugsnagUnity.Payload.User::set_Id(System.String)
extern void User_set_Id_mF089592513A8ABB3A0743C2F2044FB4C5186851D (void);
// 0x000001ED System.String BugsnagUnity.Payload.User::get_Name()
extern void User_get_Name_mAE5BB7DA4739B36ACA1F915BE6B1BBD5CA79F332 (void);
// 0x000001EE System.Void BugsnagUnity.Payload.User::set_Name(System.String)
extern void User_set_Name_mE75F551F7BC7ADBFDA640A1CBD8A14BED9BDCBBF (void);
// 0x000001EF System.String BugsnagUnity.Payload.User::get_Email()
extern void User_get_Email_m5A1F1A1FFF871A795DE4B6C5B81B0CBF715E60F0 (void);
// 0x000001F0 System.Void BugsnagUnity.Payload.User::set_Email(System.String)
extern void User_set_Email_m02E1DEFE1EC997D04B44688D5218CC67572603F2 (void);
// 0x000001F1 System.Void BugsnagUnity.Payload.User::OnPropertyChanged(System.String)
extern void User_OnPropertyChanged_mCB299B37AC0E562791466F41C83018DC98C57B6A (void);
static Il2CppMethodPointer s_methodPointers[497] = 
{
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	Bugsnag_Start_m99533D7F5D6993DC8CFE3F3A432292B8017D775D,
	Bugsnag_get_InternalClient_mF728A2D45F03ECC956D01F3762BB26F97567A51B,
	Bugsnag_set_InternalClient_m6D24736859831766E8601B676E40F78DA55F70AF,
	Bugsnag_get_Client_m670578B6828115448054052FBA99F05416BE6293,
	Bugsnag_SetApplicationState_mE6412B3B45EF1B2FAB1BD1F76AC0AAF9983C0439,
	Bugsnag__cctor_m0127759E15277885D91A47ECA3A691C7A2047FBB,
	Client_get_Configuration_mE1CB4A14B8CA8DF7E8BEFDA666559CF26E22832D,
	Client_get_Breadcrumbs_mCB7D0806282EEAE79271AA7CFCBC255B8B1D6B7C,
	Client_get_SessionTracking_m065FCB8E6518F43BDE275D0C248F97EAD48AE0DD,
	Client_get_User_m6353A2AA86F45A292A6191F580E47CC7218DC6D8,
	Client_get_Metadata_m472AB7A151D4836CA912F81E9B19F0388A55FEE6,
	Client_get_UniqueCounter_mB1E77725E2969E024C6C2689F601F80F25C2BD56,
	Client_get_LogTypeCounter_m25BC7DBF57EA6D2213A338F3F8BA72D553C118FD,
	Client_get_Delivery_mB8CD39650A2D7A92E747083002BB9374085A687C,
	Client_get_Middleware_mA5BC37B4AC39D6A0479D47F18B43544180069589,
	Client_get_MiddlewareLock_m26CB202A5465E8801632755A2877CA6AA449EB19,
	Client_get_NativeClient_mFB509FDC16CF5DC294A7E16CE27498FD065D20DB,
	Client_get_ForegroundStopwatch_m6EBF716FC66C5001040DC39E8623B92B9AC91803,
	Client_get_BackgroundStopwatch_mE3930B3A8F63D9F0BB7C7E4E82662A69C3727ECD,
	Client_get_InForeground_mDBCC0C882AFF50C6F2F7068CBB806734E3A077CF,
	Client_get_TimingTrackerObject_mA5A4E55F7456B43331337C56881DD0F93815A52C,
	Client__ctor_m7674A0E55E3F2BB5AC0BDAC207F3C611E4ED5ACC,
	Client_AddBugsnagLoadedBreadcrumb_mDF8135A7E8C39EE9DF85EDEA34EEBC3D42278387,
	Client_IsUsingFallback_m3CD71B937AD81497152D85D7DE7734334A936583,
	Client_SetupSceneLoadedBreadcrumbTracking_m5ECF7DE8D7E2A471E3312C9F3480E6BBEE90234A,
	Client_Send_m44AE255BF1C699063B6EBBE1E9A170ADDCC04E84,
	Client_SceneLoaded_m07BEF740BC8D79515AEF0499860E1B72593B19B6,
	Client_MultiThreadedNotify_m239518BF98910651E4B66F2999FBD594FB0518BB,
	Client_Notify_m327514D399381EEDBD3E7AFAA8E109776634624A,
	Client_Notify_mB1085A6AEAAE300592DA78EF4D36E0F1231BDC1E,
	Client_SetApplicationState_mDBAD009A37A58D53E23F18800992128204DFD7FF,
	Client_ShouldSendRequests_m100A8BC55D6D4BACD474B98A8B9B35E8852F3AEC,
	Client_RunInitialSessionCheck_mF16C504BED04D8295FAC6D0B7DC2529C51BA97F3,
	Client__cctor_m3CB44602EE97E07D2276A7B64176947565F0AF7D,
	Client_U3C_ctorU3Eb__47_0_m6C7C10D22C7B96377F798AFF8861F43B37211AAD,
	U3CRunInitialSessionCheckU3Ed__68__ctor_m35D94823F444DF69B573D75C07A7E36F517C3AD3,
	U3CRunInitialSessionCheckU3Ed__68_System_IDisposable_Dispose_mFEC0B489C21AAEEB37483E1C23B430B90341536E,
	U3CRunInitialSessionCheckU3Ed__68_MoveNext_m3E43F5C11C557DA47A643DA1B5CB400162781674,
	U3CRunInitialSessionCheckU3Ed__68_System_Collections_Generic_IEnumeratorU3CUnityEngine_AsyncOperationU3E_get_Current_m2C895F0EBF0D853789AEF83719CF5F2ACFF2E249,
	U3CRunInitialSessionCheckU3Ed__68_System_Collections_IEnumerator_get_Current_mED8A80EB01F06676CBA8E6AF859FE3BE67B55F0D,
	Configuration__ctor_mEFE60A6C2C34D6C2C9ABCDCCE229B1B593EDB83A,
	Configuration_get_ReportUncaughtExceptionsAsHandled_m42F8BA32514E01C6D516BC0DB19A064EA2A67C6C,
	Configuration_get_MaximumLogsTimePeriod_m1C70BBE3B59893ED943C417A2CB08931FCA9EE0E,
	Configuration_get_MaximumTypePerTimePeriod_mF8EE91F5AEF0D1CC7205C61DD1DA7EAC20F612A5,
	Configuration_get_UniqueLogsTimePeriod_m414F7AE05AC8534B70520E1F9EC31775A7658BB7,
	Configuration_set_UniqueLogsTimePeriod_mF2BA9A11C2B0F8AB46FB5BA89EAC39C2A85DE36B,
	Configuration_get_BreadcrumbLogLevel_m1F55C4834EE645CB3CB41A42C15050DAEBD80C91,
	Configuration_ShouldLeaveLogBreadcrumb_mE098E0F490561D477D048E497388F8C725938E63,
	Configuration_get_EnabledBreadcrumbTypes_m50D8B5DB11767E91488EF06341C8826E6F9928E9,
	Configuration_set_EnabledBreadcrumbTypes_mCA0B6FC29314F4C01B360E07F2356EBA864D9F35,
	Configuration_IsBreadcrumbTypeEnabled_mFADFF1482D9C3A6AA38C53873A26CE36E863D72D,
	Configuration_get_ApiKey_m26FC6FA65B0DB4CD6A6F83B851FA2BB060C8B61C,
	Configuration_set_ApiKey_mE624A03AF5FF8E9A69B49ACAD5CAD6DB3A3D284A,
	Configuration_get_MaximumBreadcrumbs_mEC7623D4D1FD10032EDC413E51749860332DD41C,
	Configuration_set_MaximumBreadcrumbs_mA17FF5791A622B71B44134BCD73D438BBDD4FA5F,
	Configuration_get_ReleaseStage_m2ED81ACD76A250975DC149FAFF3C73C13689E38A,
	Configuration_set_ReleaseStage_mFF6C01E344B52E645DACAF467070B3C1B450D3AF,
	Configuration_get_NotifyReleaseStages_mFABF309E753A66361C62D03872DFBD4A500FCB84,
	Configuration_get_AppVersion_m8325FFC8A115BDC414E966B1A0EFE477DBC16DE4,
	Configuration_set_AppVersion_mD5AA59A59225D029B55A0E263ED31BAD5EAC5291,
	Configuration_get_Endpoint_m60D0E7C89FF01F288A6DF3BC79B7BF1BDECF68E9,
	Configuration_set_Endpoint_m85162428CD2F2769B3461C7B392D08F0662F7BB9,
	Configuration_get_PayloadVersion_m2FD9B1EC68DA61CE17F3088905ECB37D3650D88E,
	Configuration_get_SessionEndpoint_m5352316D4BEC5740E59D3D7EA2D35B3496941E2A,
	Configuration_set_SessionEndpoint_m022BC04D458C87C9F05307789EB8EE47258D8BC0,
	Configuration_get_SessionPayloadVersion_m1AB4B1248611A5D81CBCEF09751F0E2695EFD6BE,
	Configuration_get_Context_m690E11626353F43A6735E450872435FABBA51B97,
	Configuration_set_Context_m277B0D1D31EB25B26C74AB78CEC8C99781FDD983,
	Configuration_get_NotifyLogLevel_m25ADEB5E47A46C5C325A032114E55727B94E9875,
	Configuration_set_NotifyLogLevel_m389B240620FDFE1D178386E497F4EEC39207C00A,
	Configuration_get_AutoDetectErrors_m3B17E05688FCCBCCF477B65C6A393277BBFE9FBD,
	Configuration_set_AutoDetectErrors_m57A6FD6CB79175D75C70DF4EC93F6F1225F57CBC,
	Configuration_get_AutoDetectAnrs_m11AF0DE8B003663CF0B02D9F89EAAC0CD294CD83,
	Configuration_set_AutoDetectAnrs_m864908E053D49180B4DD8EB8927317BBD302373F,
	Configuration_get_AutoTrackSessions_m9AA5B23720E20ED899DC59FC1E6EE10F92277448,
	Configuration_set_AutoTrackSessions_m09B00338DD907698D0BA384743BB75D6E6A9C7BB,
	Configuration_get_LogTypeSeverityMapping_m827CAB5AD722C27F92FF35EB4C3CFAEBF6335282,
	Configuration_get_ScriptingBackend_mDCA23140873BD310F5BB3C0E1B9780A70B6A39DA,
	Configuration_set_ScriptingBackend_mFCECE1CA70A539C9D4F78388DB5B0D06F1219BE0,
	Configuration_get_DotnetScriptingRuntime_m44C9119F0325AD96533F3A91C80BE46E523C9672,
	Configuration_set_DotnetScriptingRuntime_m6225859A8B86AA15181C49CEC129919C91AA52DE,
	Configuration_get_DotnetApiCompatibility_mA9F1AACD9C4A2DB7E15A3D9EE60E706F69DEB39D,
	Configuration_set_DotnetApiCompatibility_mB37C275C6543E94D289A939F164103C3902B4E38,
	Configuration_get_EnabledErrorTypes_mB43BD03300099E87056B8D2075DE5E6AD36B8624,
	Configuration_set_EnabledErrorTypes_m17778B39D3715A62D26E16AF9430A6046EF763F7,
	Configuration_IsErrorTypeEnabled_m1F42300333B172C069C8D9F62840DF3918796E52,
	Configuration_IsUnityLogErrorTypeEnabled_mEAF961167FBB5C9EABF2F32E97007B8C33C529A4,
	Configuration_IsRunningInEditor_m68E95F63E069EDDA3573171CD7D03B4061A4079B,
	NULL,
	NULL,
	NULL,
	Middleware__ctor_m3BB553AA2794FBB0600E864A45251813E4384100,
	Middleware_Invoke_m2ED3D48544E306A8B5CA1E5AF14643F7F36AF5C8,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	Delivery_get_DelayBeforeDelivery_m7AB1F79274A73E685EC5A758B665B8BC7A04EC01,
	Delivery_set_DelayBeforeDelivery_m63453B88D22082D9ADED5B763C1B6E7A2E72CF1B,
	Delivery_get_Worker_mEB9D498AA85A048FF2BA877582B999D5964220E2,
	Delivery_get_Queue_mFEA2E06811E78184514BF11A70D5437D4215A9B5,
	Delivery_get_DispatcherObject_m6E9F3F66C215E62D926C92C321424A24C8ACEAC4,
	Delivery__ctor_mA2BD0D4F49A8C972142545833DD98AA8E16C5DBF,
	Delivery_ProcessQueue_m6BCBDDA61F3AAA3131FF57A28719FFEF7D907090,
	Delivery_SerializeAndDeliverPayload_m6F8EAAE277D299F178B50229D6149214A92C6B9B,
	Delivery_Send_m14965AAB839443C0B29AE514E0A4F194ED022A8E,
	Delivery_PushToServer_mF9E95074FC95542194A64799755B3FCC3D3FE0B3,
	Delivery_CanUseThreading_m507BF1706697B2DCB2B583AE4366B5AA1FD65C5E,
	U3CU3Ec__DisplayClass17_0__ctor_m89C13BFC27C778657C3962CB8E1C9B616572167B,
	U3CU3Ec__DisplayClass17_0_U3CSendU3Eb__0_mB895F5F9E63EBAB224BF1C7AE34CCCE42BFFFE6C,
	U3CPushToServerU3Ed__18__ctor_m75CC2D5EDF2C54BAB639566CE255A6485473CC99,
	U3CPushToServerU3Ed__18_System_IDisposable_Dispose_m5C6E06B4353F2DE37D004A9D84CE8E0B941766E3,
	U3CPushToServerU3Ed__18_MoveNext_mC8215BD2F78080EFBA17F44E6D4EB13BF3398541,
	U3CPushToServerU3Ed__18_U3CU3Em__Finally1_mADEB2F11733AE86975A8CDC7635DFBF1F6957039,
	U3CPushToServerU3Ed__18_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m34D2BDDEFF50C11D52630D7578729247871C7402,
	U3CPushToServerU3Ed__18_System_Collections_IEnumerator_get_Current_mAA0C726381E2D1DC1A8FF82DFEE3710FE1DF4821,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	LogTypeSeverityMapping_get_Mappings_mA0DDF8E118E1BE5EAC7B8E16EA861053B5C493D7,
	LogTypeSeverityMapping__ctor_m1D5EA040110BBFF38E773B2A4F1632037BF2FAA9,
	LogTypeSeverityMapping_Map_m32F8FC35B72949C934CB6EB36DB2591014EE2F91,
	MainThreadDispatchBehaviour_Update_m8AE38A5667F2C9529029E619708D50588B3092C9,
	MainThreadDispatchBehaviour_Enqueue_m6625876BA5117695B5A0DF2917BC769BEBAF99D4,
	MainThreadDispatchBehaviour_Enqueue_mD927EED4B8A04F4DB74A332BEF0A5E1FB4FB6A89,
	MainThreadDispatchBehaviour_ActionWrapper_mB5C0584FF9C18465CD557F9162AF9EB0BCDA188A,
	MainThreadDispatchBehaviour_EnqueueWithDelayCoroutine_mD8D1DAFCF24647B6FE3FD3968DE8610CDD195295,
	MainThreadDispatchBehaviour_DelayAction_m6BB6F287E896D420F99D8B0082661BDFA621A4BF,
	MainThreadDispatchBehaviour_Exists_m706931ADACE843B39890B5D10D72890BCE7CF195,
	MainThreadDispatchBehaviour_Instance_m9712AFBA67F78D4D1C6AB89D690F68F2C190E23A,
	MainThreadDispatchBehaviour_Awake_mAD7FF9C5702E9D4B4F9D18D51602481C91E51E80,
	MainThreadDispatchBehaviour_OnDestroy_mC3386A0C3A5F74CCAED5F8253F73ED2B46295E07,
	MainThreadDispatchBehaviour__ctor_m3C671510C883A1FE8B2FCDEFD8E2DDE58873AC8A,
	MainThreadDispatchBehaviour__cctor_m4D158A2F4611329798433E94763728F2EB40B243,
	U3CU3Ec__DisplayClass2_0__ctor_m498D8D34BAD6E1F6DE580D599931FFA4D7EA345E,
	U3CU3Ec__DisplayClass2_0_U3CEnqueueU3Eb__0_m6C4CBB5C27D4007842192852EC18900CBDAE870A,
	U3CActionWrapperU3Ed__4__ctor_m7FBD090B1412538A4F11F3A6CF9C315713F44A03,
	U3CActionWrapperU3Ed__4_System_IDisposable_Dispose_mEFEB52C27A5C6DE82B140A52D5604027E09DD52D,
	U3CActionWrapperU3Ed__4_MoveNext_m0FE20ED2B50867F59AAD87DBAD39EB2B37FE06AC,
	U3CActionWrapperU3Ed__4_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m07B3B678F9311BA8B3D57A8C7560B07347C8372D,
	U3CActionWrapperU3Ed__4_System_Collections_IEnumerator_get_Current_mD6356946C7E7F982F72A729589B4CEBF50729420,
	U3CDelayActionU3Ed__6__ctor_m5A23537B710958B9D29EF34F395EEDCFA1EB3FB5,
	U3CDelayActionU3Ed__6_System_IDisposable_Dispose_m62BC62F4C37FADF2E03DC0796D385D4292B83DAB,
	U3CDelayActionU3Ed__6_MoveNext_mF2D598CD70803BECE9890FD521FBB6A345277955,
	U3CDelayActionU3Ed__6_System_Collections_Generic_IEnumeratorU3CSystem_ObjectU3E_get_Current_m8B0C243749E3B04ED21C796F6D30269A98FF26D3,
	U3CDelayActionU3Ed__6_System_Collections_IEnumerator_get_Current_mE56724F0F2D14816F98821EDF4397EE59758F7B4,
	MaximumLogTypeCounter_get_Configuration_m0EB736B3D884A30AA1053E6AFCF707B4C130DD34,
	MaximumLogTypeCounter_get_CurrentCounts_m086A5330C326EF35A1CCCABF59EFC33A26277CAE,
	MaximumLogTypeCounter_get_FlushAt_m622366BEA58B2431B9AD8BDA8537BB8CB46C5BFB,
	MaximumLogTypeCounter_set_FlushAt_m620CA5FB4946B6C6CCEA46C9B61B25B06319DA4C,
	MaximumLogTypeCounter_get_MaximumLogsTimePeriod_mBF7BBF11CE0786A5E388B0099485F37A16035FE1,
	MaximumLogTypeCounter_get_MaximumTypePerTimePeriod_m2C89C6DF598FA6090F520FE78DAAEB341478C073,
	MaximumLogTypeCounter__ctor_m2EDF753E06CD2254F8AAFF05CE04202BC971879E,
	MaximumLogTypeCounter_ShouldSend_m9D9950070917B55549C71868F3EB60CB0E4935D1,
	NULL,
	NULL,
	NULL,
	SessionTracker_get_Client_m761307B966EB7FFB8DFD0295780722D739DC8FAA,
	SessionTracker_get_CurrentSession_m4922443B6C0BDCBA6956B654CAEA86D382A5CB3F,
	SessionTracker_set_CurrentSession_m982E6605E9DECAB258CB7F7B4FFC6B670F939B27,
	SessionTracker__ctor_m370C865FAE59D4B7775BA1A79A297DB3B3CFCE7F,
	SessionTracker_StartSession_m13B4C0D7F8B01101B1D7FAB7684A344EA15157F1,
	SessionTracker_AddException_mC6E66C4881535DD43AE97D57DCD8C53288678F27,
	LogTypeExtensions_get_LogTypeMapping_m3EAEB788995EB06E806855955D061ADE741309C3,
	LogTypeExtensions_IsGreaterThanOrEqualTo_m0299C5A6D21DCBD0065C04B3FDE2C77607A6F249,
	LogTypeExtensions__cctor_m1AB3FA8BD5414EAC9494A2C1B9E82D46F0D3ECD0,
	LogTypeComparer_Equals_m0C42DF502C711B8655859CBDB4ADFC022287852A,
	LogTypeComparer_GetHashCode_m6A3EA1CF1D693A2BBEC09FC95F7C27C0C176D5A1,
	LogTypeComparer__ctor_m7D9E271202D5E7BAAF0D808AAE8D6508BDD45CBD,
	JsonObject__ctor_m8715AD972E621AF25134CC64D9B2F8F93629F6FB,
	JsonObject_Add_m77AC615EEE88B11CF1C203D72EDF5636E88EB65E,
	JsonObject_ContainsKey_m935805209E5750BBFA73C7DF0E87A063B0D41383,
	JsonObject_get_Keys_m31090FCD83130C0899EB173A89128FA9BD05E364,
	JsonObject_Remove_m827C0CCAA18438DAA0810D51F22E98FFCE5CCD9D,
	JsonObject_get_Values_m0F60717DFC5A56BA64268B0E93984272FADD68FB,
	JsonObject_get_Item_m3AC2A84F3684E05D102EACE56BAB32ADFA69E56B,
	JsonObject_set_Item_mAC4E4BA730E000AED0ACAC635853A3F8EB35008B,
	JsonObject_Add_m359F65AF8844A6070E776393C60062B67D1820A2,
	JsonObject_Clear_m59B6F4A7F6D4E15FCCEDCF9B0B7DE692EC3838CC,
	JsonObject_Contains_m05C4DF4EBB2B7E0C4A0E9A9CA975A81EFB8A0603,
	JsonObject_CopyTo_m8758B9F75C0B3F2B92EF9FD44966CFD783FD536F,
	JsonObject_get_Count_m5FC4D511D235E7BAFC1EA295625BE375E55105EB,
	JsonObject_get_IsReadOnly_m2CA994EC39DC8198468E98410E7B3329C1DD998E,
	JsonObject_Remove_m16B5758DAE38725910BA1AF0C922099074323E9E,
	JsonObject_GetEnumerator_m8A96038B1227596B01A7A4E5CD12BC6FFCB3B1D7,
	JsonObject_System_Collections_IEnumerable_GetEnumerator_m4666FA0C1809B612450A596004EE7150F54BE16A,
	SimpleJson__cctor_m8B0C6436294D917930ED85E06286DCBDB66C2091,
	SimpleJson_SerializeObject_m7ADC508716448076C086A159E488F054D1CDADD2,
	SimpleJson_SerializeObject_mB250869D7286B5CF6077499E4E50A6D91A02EEE6,
	SimpleJson_SerializeObject_m5D990CDBCEA328A76B5BEB29373DAC9FC5C795DF,
	SimpleJson_SerializeValue_mE10B74D19B1C3D0BA877E8305DE901BF8DF71342,
	SimpleJson_SerializeObject_m0898B20C1FDD514EA0BA161BCB14BDA3EC2EEB76,
	SimpleJson_SerializeArray_m804F1DD3C44E69291898252B9C3CD79359299E16,
	SimpleJson_SerializeString_m28B03126411582CEA245530DF3D94DBDE8A18AE6,
	SimpleJson_IntToHex_m3E699B008E7588762A1404B6D8BFC95EF7AC7154,
	SimpleJson_SerializeNumber_m10B97CA6135D5F1C04537ECDA55E9864832B3292,
	SimpleJson_get_CurrentJsonSerializerStrategy_m859209D40C5C747B3E11933FBF6E98455F677383,
	SimpleJson_get_PocoJsonSerializerStrategy_m97ECF185A0D9D054262ABAEF1A3051A62D4FCBAA,
	NULL,
	PocoJsonSerializerStrategy__ctor_mF9867692309E8D15D647BF4B4E712423177A4281,
	PocoJsonSerializerStrategy_MapClrMemberNameToJsonFieldName_m5DBF53E4E2632398CB1EF943147DCAC6E673733E,
	PocoJsonSerializerStrategy_ContructorDelegateFactory_mFF425F030AD66F7F3AD71EA4B4F474DE0B6A71FC,
	PocoJsonSerializerStrategy_GetterValueFactory_m092F20D381AE5CB587396E1282CB298D70C5F4E4,
	PocoJsonSerializerStrategy_SetterValueFactory_m5A1C6528DF48547AFDCDEDD9C573B24458C2F3A7,
	PocoJsonSerializerStrategy_TrySerializeNonPrimitiveObject_m90D2213CFC5284F7152F3F5E5F4024B59C650C75,
	PocoJsonSerializerStrategy_SerializeEnum_m62F3D96F3D835460CB276705DFA6FBA16919FE00,
	PocoJsonSerializerStrategy_TrySerializeKnownTypes_m742A2933A309DAE3150D794C8A7D3F399A23446D,
	PocoJsonSerializerStrategy_TrySerializeUnknownTypes_m364F17929AFA8801F43904C91C0E72DF80EC3278,
	PocoJsonSerializerStrategy__cctor_mD269A0BC11AE27E0C5192CE54DFCD1D9A1569B1D,
	TimingTrackerBehaviour_Awake_m37BD6D7AA1299E19CCA6A503193F932D1878B507,
	TimingTrackerBehaviour_OnApplicationFocus_mE9B711B9033367FEDA4794ED6BE00392495D5EEA,
	TimingTrackerBehaviour_OnApplicationPause_m02590C1A4ED24BB3AB2443A3A54C1972A8968816,
	TimingTrackerBehaviour__ctor_m6DE6C100133CF39757C663D7537270F45CE12F18,
	TypeNameHelper_GetTypeDisplayName_mD3E99C6C6925155C373E1DC823339B772D365D2F,
	TypeNameHelper_ProcessType_mE00F793FAE23AC3539ACA6C55EC7FFC1A9FA2423,
	TypeNameHelper_ProcessArrayType_m6DAA3B80B656C34A7F43567E88B0504F3D928BD7,
	TypeNameHelper_ProcessGenericType_m6199373973ECD0B0A1C46DAAC0A3247977CF2333,
	TypeNameHelper__cctor_m111125B22DAC9E6D2D17B2DC5FC9D07BE31E5825,
	DisplayNameOptions__ctor_mAA8EBA9CC2C017D61DE68D885761634143F8E4B8,
	DisplayNameOptions_get_FullName_m6F83DDB661DBEEDF4D7370998D35E1CD33D112FD,
	DisplayNameOptions_get_IncludeGenericParameterNames_m2F98661BD8E5EE7BBDD878D1CC76A17484CE939D,
	UniqueLogThrottle_get_Counter_m66D5FE6EF0EB844457A3392DB04C44118CBE3CCC,
	UniqueLogThrottle_get_FlushAt_m76C994BCC40D5113FDA528CE962CC4C71C5F9613,
	UniqueLogThrottle_set_FlushAt_mAB8D168D035958FD9FD8C834ABCD396508C94BC5,
	UniqueLogThrottle_get_Configuration_mAA388C09A356092A891F16BC661403F995227FE2,
	UniqueLogThrottle_get_UniqueLogsTimePeriod_mDB0DC8D80E795CD820DC17AFBBBB593E3FBF9617,
	UniqueLogThrottle__ctor_mE334191510C60134778C189E2944D279521EDEEF,
	UniqueLogThrottle_ShouldSend_m1CCB7C79E66F43FB2135831EBA0830B385A913B4,
	UnityLogMessageEqualityComparer_Equals_mDF590786C5D2192263FE8C51E8F56A97707CB837,
	UnityLogMessageEqualityComparer_GetHashCode_m30D74C0D772B6CD9AFCD041ACDF4862AF815CCF0,
	UnityLogMessageEqualityComparer__ctor_mC1AB1A9B8A286A1594DE740B1A7040029FC23723,
	UnityLogMessage__ctor_mBB64CD8A956F91FA2E36D37AAACB1F00AA6C10B0,
	UnityLogMessage_get_Condition_mB46AE016A06998547F14B993E8A5D5DEF6D6E4F7,
	UnityLogMessage_get_StackTrace_mB876FA320B6359901B52F1DD95B0A4149DCFDFC0,
	UnityLogMessage_get_Type_m65C37D6FD98A76785B019CC631EDF79BB32F5B70,
	UnityLogMessage_get_CreatedAt_m7D4861060208600C566FE9516B98E20DA323646B,
	UnityMetadata_InitDefaultMetadata_m032E205B70564926B63E9021EA3CBCEDD0979738,
	UnityMetadata_InitAppMetadata_m38FA19D680860423CFBA4F011C30611260894313,
	UnityMetadata_InitDeviceMetadata_m91E3A7A50353D4C043B3E70E0F26B7FAE21C0B42,
	UnityMetadata__cctor_m7B4CE8E524887FD9748127FF34382B4D05C391FF,
	Breadcrumbs_get_NativeInterface_m14E86EF4ABEC5BA26F3DDCE7E677E186F6BA2BB3,
	Breadcrumbs__ctor_mFC66C2A97F79EFCCFC8293670D8ABFAF52BB31F4,
	Breadcrumbs_Leave_m0D8CD8A994A625D941B31AA6A8D46886A8A0E6E9,
	Breadcrumbs_Leave_m565C3E2E86FED49F5F224BC9519620EFFACCE421,
	Breadcrumbs_Retrieve_mCB1B4EE0A6D290A85C671BD112A07550A9FC405F,
	NativeClient_get_Configuration_m7B67AB080D98E10EB3E21DF3D466BE4FBA2A35C4,
	NativeClient_get_Breadcrumbs_mA88F13C4F531B76CAED74F4CB4EF0AE01AFAEF0F,
	NativeClient_get_Delivery_mE403E7CB048043F43379AA7585F12CE8B413EEC2,
	NativeClient__ctor_m0EDB27652695C65C87F9E9D1AA199C0D8DC85AAC,
	NativeClient_PopulateApp_m968E534FB07DAFEF285CB32A76D8A74DFDFD4AE0,
	NativeClient_PopulateDevice_mBA0E9F982AB5CD88E677EF577DC34DC8A4D91098,
	NativeClient_PopulateUser_m90703C14136F1EA708921B56339FD6C11D3ED284,
	NativeClient_SetMetadata_mD0391D3C25D59ED0F727DBE83E9AA5AB98374274,
	NativeClient_PopulateMetadata_m8EE3D2777634829EF897FF5711352DF64A8CB75C,
	NativeClient_MergeDictionaries_m42DDF6BBC2ABE6857C2A9D9988A648BA615FB772,
	NativeClient_SetSession_m5B18F5600E5599D7EA3CBE1FBF2FA2DBDF403E04,
	NativeClient_SetUser_m4BC41A2DFE4421F3FCC5E42AC5BFD11CD20BAB66,
	NativeClient_SetContext_mDDCBFFA67E2CC6F6F9A37F4E180B09B5EDF550C4,
	NativeInterface__ctor_m49D7A875A7E68FD0E3DFE6EADE440757E8A5D880,
	NativeInterface_CreateNativeConfig_mC9146C1B42D424892CF3E413D9936C9A44BFDB44,
	NativeInterface_ConfigureNotifierInfo_m84D6741E63A8BDA7B7AE4C7077EF4986BB84633A,
	NativeInterface_PushLocalFrame_mB85016D26D437AA544D033FA984A4D7BDFAECE48,
	NativeInterface_PopLocalFrame_m09A8B357A96F47064EB2A9BF7E8C7CA9B3132878,
	NativeInterface_SetContext_mB8B4FD5D85FE856D72B6A172A626AF88E404C2BD,
	NativeInterface_SetUser_m780B17C7C2FD5DB2C8FB32C0941D56F1A6E206C6,
	NativeInterface_SetSession_mE41D506428512530DD9D42628B2AEC57118D9547,
	NativeInterface_GetApp_mEBEF5C1760EDD7F9EF9A8797A3A1D292A5D78B80,
	NativeInterface_GetDevice_m05CF7C915DB2F6AC4E2D6A858A976379358C6F16,
	NativeInterface_GetMetadata_mCF660982F58F7369EDC8228AD23455164120CC0C,
	NativeInterface_GetUser_m961A8EDE1F107A93595E5A28D8FB207923CB3605,
	NativeInterface_RemoveMetadata_m1B0074AF1F501694BED8B8DD18F1FD8580398025,
	NativeInterface_AddToTab_mFE4E83C4867787EE06DA4469260B3F40FA1504E4,
	NativeInterface_LeaveBreadcrumb_mC999007B6983EA5FC27FD216D7F490AF1E3EEDC5,
	NativeInterface_GetBreadcrumbs_m425A40027D35DBF036F09771D02A216A62432ED3,
	NativeInterface_GetJavaMapData_m5FFAD419C6C4E97E40E3AFA785FB88AEF4FF99E1,
	NativeInterface_ConvertStringArgsToNative_m810B1DCB9BF1EB1ED1BDCED46CC9DC365E377D17,
	NativeInterface_ReleaseConvertedStringArgs_m23D17193044F778D81C948772DC00740A98BEE22,
	NativeInterface_CallNativeVoidMethod_m7BE079D1204CBA64F289545F50B1CAB4434F5981,
	NativeInterface_CallNativeObjectMethodRef_m648989F4A1B918BA3B991A9A01DD2E5F580FD037,
	NativeInterface_bsg_unity_isJNIAttached_m0A825C66EE7AB66DBF8E8EDD37CDE51F825449F5,
	NativeInterface_ConvertToBreadcrumb_m79CA3711D83F7E1C4098CBE97DEFBB2C519874A8,
	NativeInterface_BuildJavaMapDisposable_m41C81E08629E8AAC25A8DFBDA9D43F2BD2A887BA,
	NativeInterface_BuildJavaStringDisposable_m341176AF5328E1E59C2C71D51C91AB3A48973BC3,
	NativeInterface_IsUnity2019OrNewer_mF18DCC4BB85B2B2C2FFEABF29EC7ABFC4377120E,
	NativeInterface_CanRunJNI_mC12DE0B2F5D8DBF5C47EB76A3204FA4D76913A78,
	NativeInterface_DictionaryFromJavaMap_m995993546F0E94C8B6F812C7A40FF014B02336CE,
	ReflectionUtils_GetTypeInfo_m76CE7EB54A118E3FE726DB364C81EB90874AA39E,
	ReflectionUtils_IsValueType_m2EFDC263358CF0BCC693DD1EB929AB665A80F1E9,
	ReflectionUtils_GetConstructors_m083A55CB9BAF61C0CB26A700D122F15AC2061677,
	ReflectionUtils_GetConstructorInfo_m20A885BDE5699F098BA4B6F30296E617E69A85CA,
	ReflectionUtils_GetProperties_mE05CCFCE775C04BC28296C4C5DE681AA8C41340D,
	ReflectionUtils_GetFields_mFFCD16CCDF3EAF3E62003F1401A010FA6C13D63D,
	ReflectionUtils_GetGetterMethodInfo_m7673CD82B21B61DD47B701A1776523D4C009E88C,
	ReflectionUtils_GetSetterMethodInfo_m6318D907AB552B3CAF51EB8AF6206FBA8842D1AA,
	ReflectionUtils_GetContructor_mB0ECD6B627A84F3036A10E7A72428408117188E2,
	ReflectionUtils_GetConstructorByExpression_mB06D847143EB25B5735B2E79308AB027E527034C,
	ReflectionUtils_GetConstructorByExpression_m5FD42036FF56A1028F243E73F3C51D92F385A612,
	ReflectionUtils_GetGetMethod_mC3AF560399337731C6F715795014FFEA6DF186FD,
	ReflectionUtils_GetGetMethod_mC897E931904F576E11B7578250C2B47E7648E3BA,
	ReflectionUtils_GetGetMethodByExpression_m2C27434FAB45F2576098F0966F587E3A4BB56A3B,
	ReflectionUtils_GetGetMethodByExpression_m80CB7DCF97A728203A967800C949BE3B9F5204C6,
	ReflectionUtils_GetSetMethod_m22297555552DE04979E9B8A550A0B19A7BCFA107,
	ReflectionUtils_GetSetMethod_m855412286F8FA211FDF9101CDC7B2B83A0650A4A,
	ReflectionUtils_GetSetMethodByExpression_mE2C7AD016142BCDF20FAAF81C0918A41DA0C13D6,
	ReflectionUtils_GetSetMethodByExpression_m6F422BAF2A66DDCD1F39A32B9EEB1DFB414341E9,
	ReflectionUtils_Assign_m83081BE19A26CA7D1E02F49EC45072056ADCF7CC,
	ReflectionUtils__cctor_mF3B5CD75BB2FEC95CE35BA7823D378A99787E293,
	GetDelegate__ctor_mA7FBD3844BCEB926506C50FAB4251FE1581F7F1E,
	GetDelegate_Invoke_mC8AB5516BAA6E4AE36B7DECB66A45599A009DBF5,
	SetDelegate__ctor_mDBA506C897F81D05877D6F8E2810C652F81AB91E,
	SetDelegate_Invoke_mAD5E00776C46ECBBEE83A9892031AD475E168D4E,
	ConstructorDelegate__ctor_mBED41D9B1F7BF383D9C255701B611AF3C32C3372,
	ConstructorDelegate_Invoke_m93DFB0E4B38CBA68CF262EF01AE72791F701FC10,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	U3CU3Ec__DisplayClass27_0__ctor_m9ED512FA1ACEC5EDA49B1285BDACAA349BC6E63A,
	U3CU3Ec__DisplayClass27_0_U3CGetConstructorByExpressionU3Eb__0_m9C0447B805A8F17B003F31BF6BC5B38AB94AB662,
	U3CU3Ec__DisplayClass33_0__ctor_mB7EBCE104FDCB78CB491A50B0D737314B5E3E08F,
	U3CU3Ec__DisplayClass33_0_U3CGetGetMethodByExpressionU3Eb__0_m77A1BA4BDB9A46F473B2286AB2A131E58EE26C87,
	U3CU3Ec__DisplayClass34_0__ctor_mCB3D4CFB698F162A1334B99A388AC2508C290F7B,
	U3CU3Ec__DisplayClass34_0_U3CGetGetMethodByExpressionU3Eb__0_m4C89BBBAEB6318CAA2CB8AEC7AADA98057563A1A,
	U3CU3Ec__DisplayClass39_0__ctor_m9CCE1032B8D57EA3D364275DA5CBE0E17A90C0A8,
	U3CU3Ec__DisplayClass39_0_U3CGetSetMethodByExpressionU3Eb__0_m6D1332BA68455980426DC8099BD3176F5DE6D8A4,
	U3CU3Ec__DisplayClass40_0__ctor_m1342687ACA4B2ADD1320D68860D74010B9B85E8D,
	U3CU3Ec__DisplayClass40_0_U3CGetSetMethodByExpressionU3Eb__0_mECB5D755E37E618F77C7F58A82DA2CB506686101,
	String_IsNullOrWhiteSpace_mC172FD901004D8C0F372C212929C4D061095342F,
	App__ctor_m05F5B88D6BCA2B390DCB0ACE93B4DEF1C2045203,
	App_set_Version_mAEAAEEA79540CC7E878914514A9B2C7AA18BBD0C,
	App_set_ReleaseStage_m089DF4E99CD95574F4BB90B8B571C84BD8B62674,
	App_set_InForeground_mCADCF6CED9059D8ADE750E94B68539DA96698A69,
	App_set_DurationInForeground_m89BA978D8E56AD8E2AFA94EE9D6128DBF785084B,
	Breadcrumb_FromReport_m1F5BF1ABC1AFBFD1F174F6D845827C230234864D,
	Breadcrumb__ctor_m857F339E0113D68323D198A1473F588F4B66A9FD,
	Breadcrumb__ctor_mEB8657A8298A18A52324AF34D0D483174800F011,
	Breadcrumb_get_Name_m2BFB55C0F4C80549EF4BA43D3D8B5B68BFF134DF,
	Breadcrumb_get_Type_m2E64022FFF88EA7C848B47582342E5795E0D2AEE,
	Breadcrumb_get_Metadata_m7AB19B3E9A424D622A606401523F39DA7E863142,
	Device_InitUnityVersion_m64F4C20FFB719B3E3A5F48DB00F4D25EB4C4803D,
	Device__ctor_mFC6FAD7C418C5BADEC0E5B42F3F0D1E7B9C62411,
	Device__ctor_mB31CE70F695910C31457B019244E4652B05A9672,
	Device_AddRuntimeVersions_m39F755547A2CCB49E286DF90BE82AAF7AB487751,
	Device_get_Hostname_m7F995122AC544B8D09440214F625D964D8291A19,
	Device_get_OsName_m4E47A2702D50ECF537B2822A190415D6C09D3DB6,
	Event__ctor_mEBFC7B6B89B61C6986879B02CFE9F797F086D64B,
	Event_get_Metadata_m9A99A62619DD1ECF7831A084D087232BDB614D37,
	Event_get_IsHandled_m35E3B31E4E2EF423A7C41F32438D518CC8F8F41C,
	Event_get_Context_mF2EFDEEE22C1F57EAD793E996272D08888F75460,
	Event_get_Exceptions_mE65361063C29C44FFE06A432291B2B5394AD337B,
	Event_set_HandledState_m9C8BE0A1D7CB9AA7AB26959C7686A0D0F587B328,
	Exception_get_HandledState_m14334619113E50A17993AEF6ABB897633CC0F4B3,
	Exception__ctor_mA062DE928989F4832F1024D802C62CBF7B2EDB77,
	Exception_get_ErrorClass_mEC075C48F95B9263F0A560086BE93BAABA20C44F,
	Exception_get_ErrorMessage_m993AF3F983F8CE7AD0FFBA00FE516743700D408F,
	Exception_FromUnityLogMessage_m27AD01566A06B28E6A7A48EE4607B5DFB4DBB4C3,
	Exception_ShouldSend_m01BD69F77961C044F8091854259032642CC81716,
	Exception__cctor_m9BC419DC4816C4E38EB7EB89DD7CC231B87938C3,
	HandledState_ForUnhandledException_m6790D7D905DB0A97F7F975A53AF1701D6D1FC9C4,
	HandledState_ForUnityLogMessage_m7837D6EE958C30FC2F340FBD5594ABBF3307C7B2,
	HandledState__ctor_m785F272C2202C8711E478D4DF50024DDA5E9CEFB,
	HandledState_get_Handled_m3B353FFF6FE103AD676E629ED61A31FC7C408C90,
	SeverityReason_ForUnhandledException_mAE0F11A291E9B71F32D11E554081A9757DD2A18C,
	SeverityReason_ForHandledException_m50CB622ECDCCA01D25CCBBEDC377C1DDB5F8A13E,
	SeverityReason__ctor_m8AA3CD14300711196A769D62A802DA7EEDBA0B33,
	NULL,
	NULL,
	Metadata__ctor_mF4D68CBC6B97FC64C61D86F6479CD3FF18DD293D,
	Metadata__ctor_m4F5F4F12F06CBEB2429A3E135E0312F6ABC81D40,
	Method__ctor_m0EB6B66BBFDA262CE06EA155AF8D233F7D1B1A12,
	Method_DisplayName_m29B9BCC938AC7B51F280BADF85AF60F87D12271E,
	U3CU3Ec__cctor_m61D88ADEF2C1283F2DA74B6AA18C20AC85667841,
	U3CU3Ec__ctor_mC4046C2673B89C6E147A7C0F6113A7D7AB208D74,
	U3CU3Ec_U3CDisplayNameU3Eb__2_1_m65AD70F846DD2B050B6430488F7DB5EF1FFB124F,
	U3CU3Ec_U3CDisplayNameU3Eb__2_0_mBF5EDB23CEC3E0D06718D4A0D351489AB35AC15D,
	MethodParameter__ctor_mD5965B64AECD0E08FD27382DD12C9384224D1114,
	MethodParameter_DisplayName_m149CA20BDC55C9B9B65884E1D998980C356D2698,
	NotifierInfo_get_Instance_m9E36CCDF62776C1233273D17F2715747FBA408FE,
	NotifierInfo__ctor_m941F4574693D324A8C9CE4BD179194D8F3791F0B,
	NotifierInfo__cctor_m70812A17E3F1636B8362D22D19B75C25BB75B5D9,
	NULL,
	NULL,
	Report_get_Configuration_m6B75DC8894AB7A3750D72F7E0979E7EB454001CF,
	Report_get_Endpoint_m1350AF0815A2FA8C46E2F62F8A3E99E28B02D363,
	Report_get_Headers_mC50A503F8A98CCA4105803F5A29460E23DD9E89B,
	Report_get_Ignored_mA6144148B345C13E6BACFB2BB86D89125C73933E,
	Report_set_Ignored_m4DFEB6B9CFD98B28C6E5EE353DD1151EA9F1B1B7,
	Report__ctor_m512FD47A9A4D43F3AD37B13A4DEB9739A6C4D71F,
	Report_get_Event_mF229911E30E61DDF9A9DCA7E76A43D17BCF9F545,
	Report_get_IsHandled_mDD3B76C3145CA8310E4C05AA87EAFB9F073E4E4C,
	Report_get_Context_m9DB8ECF7CF6896E1644DCE01BC0D2D319B00F9CB,
	Report_get_Exceptions_m49EC7204681F47EA1EDAE6A57DA1BF41F7DF147F,
	Session_get_Id_m9CA25A2B0A621518A6402EEFFA2505D21C4313E1,
	Session_get_StartedAt_m2F9B6BCFDC8BC04BA4A5BCD0884CFD240FBDAC9B,
	Session_HandledCount_m1D9835FEC2320E32DCCB97D4A90A2823E43A823C,
	Session_UnhandledCount_mD60A7A0CEAC55E78384C162444B35C743817FCE7,
	Session_get_Events_m0E3FE99AE1A4467B72F5589708AD939CA58D1C0C,
	Session_get_Stopped_m299B3B54036C435590D941F7A02BF90AE6DB8EF1,
	Session__ctor_mFE2011D28D4649DBA0017FC3C78A911316962277,
	Session__ctor_m68E35AF8D8D1FC62B2C89B3057DEB4E29C45AC22,
	Session_AddException_m5130136710F2043413C6B1FC3E4E6AB5BD9C5B54,
	Session_Copy_m43DA6299C8071371CB20799BD0FB88E0B0719F95,
	SessionEvents__ctor_mB0CACD328B0433BDB49DF4D9A1BDBBF764EE58CD,
	SessionEvents_get_Handled_m3FEE3202381E8F712F63695978C3B2C9B2AAE9C5,
	SessionEvents_get_Unhandled_m9F5D16CF84F8468DF636FBFF5B40416D1F8E6A41,
	SessionEvents_IncrementHandledCount_m3BB460E38D2A6E8E4356FA41E100FD18FB60103E,
	SessionEvents_IncrementUnhandledCount_m3A9A71A762D7D6028B3852D93233697ADC306DD0,
	SessionReport_get_Configuration_m503B70A91414EFCD90B51A510449927468F5C96D,
	SessionReport_get_Endpoint_m3706C4F3CE4D08EFDD0F62A0DE371AD6A4E68B14,
	SessionReport_get_Headers_mB9E24EEAE379D278E8B1B0A5A4B9501C0829C5FD,
	SessionReport__ctor_mC828FFBF592CCB63982DD86D45A3A64FE8C5337B,
	Session__ctor_m568E0F33FBDE6CC46B520C2F35AD28DE33786234,
	StackTrace_get_StackTraceLines_mC9E0984F4B0053561181F8E4E0244D6BCBD1F93B,
	StackTrace__ctor_m9B06A5E7E121F23E200021D98C934FFC4EB51530,
	StackTrace__ctor_m65C5617D92937BB143249F55E306DB6D6BE3DE11,
	StackTrace__ctor_mE9CCD3E8523EC699E1F65AA798A37F169CFEB27D,
	StackTrace_GetEnumerator_m4DA074B85E788D1D0BBFAA84103E06D3762AD8CD,
	StackTrace_System_Collections_IEnumerable_GetEnumerator_m96E6FDB477C7AC99C8F58977101DAE8C001D6AB2,
	U3CGetEnumeratorU3Ed__6__ctor_m32D557775744F7F30BBD17BECA5F026AD72280F2,
	U3CGetEnumeratorU3Ed__6_System_IDisposable_Dispose_m3B0572963C30560609A024B021855108FC6FACC7,
	U3CGetEnumeratorU3Ed__6_MoveNext_m41DAC7A7666993BD6E41463D85853BCE515462F3,
	U3CGetEnumeratorU3Ed__6_System_Collections_Generic_IEnumeratorU3CBugsnagUnity_Payload_StackTraceLineU3E_get_Current_mDD4867E948964D769EEF06800DDB46A6BFDE5E1B,
	U3CGetEnumeratorU3Ed__6_System_Collections_IEnumerator_get_Current_mC8BAA0A2479397B9177BFDAB052533906BAAC6F8,
	StackTraceLine_get_StackTraceLineRegex_mD2F1CC5ACECCBA418AC4729CB9935D60AB0865B4,
	StackTraceLine_get_StackTraceAndroidJavaLineRegex_m6039AF2D8DFF743BBFBA39DA84A9D172819A5B02,
	StackTraceLine_FromLogMessage_m1719C51CC4D9B915838D4D6EA5F14944F0F1F9F7,
	StackTraceLine_FromAndroidJavaMessage_m7A0899EFCAC52A9A2BA6174DC74F13E7CFF46865,
	StackTraceLine_FromStackFrame_m891FF0BC1BA5DA3C546932D6E7315A4D3985BCBA,
	StackTraceLine__ctor_mC1A1ACF90106E07CC1F78E9C814BE3541D4BF6B7,
	StackTraceLine__cctor_m4DFC40103A010D154F8F32281D0933779A7E3E67,
	User_add_PropertyChanged_m635DCF6846D2A662BFF8059596339E9DFE05DD88,
	User_remove_PropertyChanged_mC13C19DEE2C57B5B8B54DE039EA607D1FEB2BA5A,
	User__ctor_mD4A56113C5A9468726C5A253C2F9FA25DFE1A89F,
	User_get_Id_mC5D896375444EE67AD9152733F2914619754E597,
	User_set_Id_mF089592513A8ABB3A0743C2F2044FB4C5186851D,
	User_get_Name_mAE5BB7DA4739B36ACA1F915BE6B1BBD5CA79F332,
	User_set_Name_mE75F551F7BC7ADBFDA640A1CBD8A14BED9BDCBBF,
	User_get_Email_m5A1F1A1FFF871A795DE4B6C5B81B0CBF715E60F0,
	User_set_Email_m02E1DEFE1EC997D04B44688D5218CC67572603F2,
	User_OnPropertyChanged_mCB299B37AC0E562791466F41C83018DC98C57B6A,
};
extern void DisplayNameOptions__ctor_mAA8EBA9CC2C017D61DE68D885761634143F8E4B8_AdjustorThunk (void);
extern void DisplayNameOptions_get_FullName_m6F83DDB661DBEEDF4D7370998D35E1CD33D112FD_AdjustorThunk (void);
extern void DisplayNameOptions_get_IncludeGenericParameterNames_m2F98661BD8E5EE7BBDD878D1CC76A17484CE939D_AdjustorThunk (void);
static Il2CppTokenAdjustorThunkPair s_adjustorThunks[3] = 
{
	{ 0x06000102, DisplayNameOptions__ctor_mAA8EBA9CC2C017D61DE68D885761634143F8E4B8_AdjustorThunk },
	{ 0x06000103, DisplayNameOptions_get_FullName_m6F83DDB661DBEEDF4D7370998D35E1CD33D112FD_AdjustorThunk },
	{ 0x06000104, DisplayNameOptions_get_IncludeGenericParameterNames_m2F98661BD8E5EE7BBDD878D1CC76A17484CE939D_AdjustorThunk },
};
static const int32_t s_InvokerIndices[497] = 
{
	-1,
	-1,
	-1,
	-1,
	-1,
	1727,
	1784,
	1766,
	1784,
	1768,
	1792,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1037,
	1022,
	886,
	1043,
	1037,
	1043,
	886,
	577,
	365,
	365,
	235,
	898,
	1037,
	1022,
	1792,
	568,
	877,
	1043,
	1037,
	1022,
	1022,
	886,
	1037,
	1040,
	1022,
	1040,
	902,
	1012,
	780,
	1022,
	886,
	780,
	1022,
	886,
	1012,
	877,
	1022,
	886,
	1022,
	1022,
	886,
	1022,
	886,
	1022,
	1022,
	886,
	1022,
	1022,
	886,
	1012,
	877,
	1037,
	898,
	1037,
	898,
	1037,
	898,
	1022,
	1022,
	886,
	1022,
	886,
	1022,
	886,
	1022,
	886,
	780,
	780,
	1037,
	360,
	886,
	1022,
	567,
	886,
	898,
	1022,
	1037,
	780,
	780,
	1040,
	1022,
	780,
	780,
	1022,
	1040,
	1012,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	1022,
	886,
	1012,
	1037,
	1037,
	1037,
	1022,
	1022,
	1022,
	1022,
	886,
	1037,
	898,
	1022,
	1022,
	1022,
	1043,
	1043,
	886,
	886,
	433,
	1037,
	1043,
	1043,
	877,
	1043,
	1037,
	1043,
	1022,
	1022,
	1022,
	1022,
	1022,
	886,
	886,
	568,
	886,
	886,
	886,
	886,
	886,
	1022,
	1043,
	666,
	1043,
	886,
	886,
	717,
	571,
	435,
	1788,
	1784,
	1043,
	1043,
	1043,
	1792,
	1043,
	1043,
	877,
	1043,
	1037,
	1022,
	1022,
	877,
	1043,
	1037,
	1022,
	1022,
	1022,
	1022,
	1002,
	868,
	1040,
	1022,
	886,
	792,
	1043,
	1022,
	886,
	1022,
	1022,
	886,
	886,
	1043,
	886,
	1784,
	1578,
	1792,
	454,
	666,
	1043,
	1043,
	568,
	792,
	1022,
	792,
	1022,
	717,
	568,
	855,
	1043,
	748,
	565,
	1012,
	1037,
	748,
	1022,
	1022,
	1792,
	1199,
	1634,
	1471,
	1116,
	1086,
	1116,
	1586,
	1622,
	1586,
	1784,
	1784,
	460,
	1043,
	717,
	717,
	717,
	717,
	460,
	717,
	460,
	460,
	1792,
	1043,
	898,
	898,
	1043,
	1393,
	1473,
	1473,
	1197,
	1792,
	576,
	1037,
	1037,
	1022,
	1002,
	868,
	1022,
	1040,
	886,
	792,
	464,
	675,
	1043,
	365,
	1022,
	1022,
	1012,
	1002,
	1792,
	1792,
	1792,
	1792,
	1022,
	886,
	360,
	886,
	1022,
	1022,
	1022,
	1022,
	886,
	886,
	886,
	886,
	568,
	886,
	568,
	886,
	886,
	886,
	886,
	717,
	886,
	1037,
	1043,
	886,
	886,
	886,
	1022,
	1022,
	1022,
	1022,
	886,
	366,
	366,
	1022,
	717,
	717,
	568,
	366,
	280,
	1788,
	716,
	717,
	717,
	1037,
	1037,
	716,
	1727,
	1743,
	1727,
	1552,
	1727,
	1727,
	1727,
	1727,
	1552,
	1727,
	1552,
	1727,
	1727,
	1727,
	1727,
	1727,
	1727,
	1727,
	1727,
	1552,
	1792,
	567,
	717,
	567,
	568,
	567,
	717,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	-1,
	1043,
	717,
	1043,
	717,
	1043,
	717,
	1043,
	568,
	1043,
	568,
	1743,
	886,
	886,
	886,
	898,
	902,
	1727,
	237,
	360,
	1022,
	1022,
	1022,
	1792,
	1043,
	886,
	886,
	1784,
	1784,
	20,
	1022,
	1037,
	1022,
	1022,
	886,
	1022,
	237,
	1022,
	1022,
	1256,
	1743,
	1792,
	1784,
	1724,
	382,
	1037,
	1784,
	1784,
	568,
	1022,
	1022,
	1043,
	886,
	886,
	1022,
	1792,
	1043,
	717,
	717,
	886,
	1022,
	1784,
	1043,
	1792,
	-1,
	-1,
	1022,
	1022,
	1022,
	1037,
	898,
	568,
	1022,
	1037,
	1022,
	1022,
	1009,
	1002,
	1012,
	1012,
	1022,
	1037,
	1043,
	331,
	886,
	1022,
	519,
	1012,
	1012,
	1043,
	1043,
	1022,
	1022,
	1022,
	141,
	568,
	1022,
	886,
	886,
	565,
	1022,
	1022,
	877,
	1043,
	1037,
	1022,
	1022,
	1784,
	1784,
	1727,
	1727,
	1727,
	353,
	1792,
	886,
	886,
	1043,
	1022,
	886,
	1022,
	886,
	1022,
	886,
	886,
};
static const Il2CppTokenRangePair s_rgctxIndices[4] = 
{
	{ 0x02000002, { 0, 7 } },
	{ 0x02000031, { 7, 14 } },
	{ 0x060001B6, { 21, 2 } },
	{ 0x060001B7, { 23, 1 } },
};
extern const uint32_t g_rgctx_Queue_1_t89D63E3C17B498483EE52343D52166E21E23A37A;
extern const uint32_t g_rgctx_Queue_1__ctor_m5EAFB471B12D5B189C05FBEFF729D5AC19B2AFCA;
extern const uint32_t g_rgctx_BlockingQueue_1_get_QueueLock_mB27A672072400F2691684449ACA1768D7A27734A;
extern const uint32_t g_rgctx_BlockingQueue_1_get_Queue_m5EC28ADD98AC18D8C12D197E030E51A48C5C31B9;
extern const uint32_t g_rgctx_Queue_1_Enqueue_mB5E5594A8232B38273D74DC22EA889756887259E;
extern const uint32_t g_rgctx_Queue_1_get_Count_mC69E746111D67578A67ABFAFD425ACD00EE7C53D;
extern const uint32_t g_rgctx_Queue_1_Dequeue_m653CF2A66E80451A105E03FE8172FDA5901B6CA4;
extern const uint32_t g_rgctx_ThreadSafeDictionary_2_AddValue_m19324E1265296904165541776EAA5BF802A8205C;
extern const uint32_t g_rgctx_Dictionary_2_TryGetValue_mBB06F36F00031AEB2AF4144E10DFA3FBDE620831;
extern const uint32_t g_rgctx_ThreadSafeDictionaryValueFactory_2_Invoke_mF7170230311A48DCBEC2DF0C75F825D9FA9A54CD;
extern const uint32_t g_rgctx_Dictionary_2_tDFBA47563A12D1C9D68AF57E6376440AE82C8CCA;
extern const uint32_t g_rgctx_Dictionary_2__ctor_mA3BE83166E28FF8A5FB42F66FFD97629DD5274F0;
extern const uint32_t g_rgctx_Dictionary_2_set_Item_m45683B684A82A00870CCF863720B4A07D33D94E8;
extern const uint32_t g_rgctx_Dictionary_2__ctor_mAA5B2CC4F8B86A8C15FD65069744515C61011EF2;
extern const uint32_t g_rgctx_Dictionary_2_ContainsKey_m5E18B48EB584999464C490D750FC465A427D68CB;
extern const uint32_t g_rgctx_Dictionary_2_get_Keys_mBB1CB9322573D60ECC28D56AD86685F551DC1E18;
extern const uint32_t g_rgctx_Dictionary_2_get_Values_m0123C2EA761545B4B15DFEB39F248357B17F9EBE;
extern const uint32_t g_rgctx_ThreadSafeDictionary_2_Get_mC8BCAD66843D06D49B43236D7226C7C00AE9C2BF;
extern const uint32_t g_rgctx_Dictionary_2_get_Count_m52F7503B30AAD48420E4727CAC11AB7C85844580;
extern const uint32_t g_rgctx_Dictionary_2_GetEnumerator_m6E354AFF0AAB0E5F794A1D5FE44A3238F2687970;
extern const uint32_t g_rgctx_Enumerator_tDCD23637407D83E7BA34F7D8555A272FFB90DE0B;
extern const uint32_t g_rgctx_T_t2787678B008E103B08A08145F5388094F34CD6BB;
extern const uint32_t g_rgctx_IDictionary_2_t15F2148A421F838EF638904D3CCB78C1A81D4260;
extern const uint32_t g_rgctx_Dictionary_2_TryGetValue_m1396B85B87A7D7DC104EA1F3ADA899CFCE322F05;
static const Il2CppRGCTXDefinition s_rgctxValues[24] = 
{
	{ (Il2CppRGCTXDataType)2, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Queue_1_t89D63E3C17B498483EE52343D52166E21E23A37A },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Queue_1__ctor_m5EAFB471B12D5B189C05FBEFF729D5AC19B2AFCA },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_BlockingQueue_1_get_QueueLock_mB27A672072400F2691684449ACA1768D7A27734A },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_BlockingQueue_1_get_Queue_m5EC28ADD98AC18D8C12D197E030E51A48C5C31B9 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Queue_1_Enqueue_mB5E5594A8232B38273D74DC22EA889756887259E },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Queue_1_get_Count_mC69E746111D67578A67ABFAFD425ACD00EE7C53D },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Queue_1_Dequeue_m653CF2A66E80451A105E03FE8172FDA5901B6CA4 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_ThreadSafeDictionary_2_AddValue_m19324E1265296904165541776EAA5BF802A8205C },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_TryGetValue_mBB06F36F00031AEB2AF4144E10DFA3FBDE620831 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_ThreadSafeDictionaryValueFactory_2_Invoke_mF7170230311A48DCBEC2DF0C75F825D9FA9A54CD },
	{ (Il2CppRGCTXDataType)2, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_tDFBA47563A12D1C9D68AF57E6376440AE82C8CCA },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2__ctor_mA3BE83166E28FF8A5FB42F66FFD97629DD5274F0 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_set_Item_m45683B684A82A00870CCF863720B4A07D33D94E8 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2__ctor_mAA5B2CC4F8B86A8C15FD65069744515C61011EF2 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_ContainsKey_m5E18B48EB584999464C490D750FC465A427D68CB },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_get_Keys_mBB1CB9322573D60ECC28D56AD86685F551DC1E18 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_get_Values_m0123C2EA761545B4B15DFEB39F248357B17F9EBE },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_ThreadSafeDictionary_2_Get_mC8BCAD66843D06D49B43236D7226C7C00AE9C2BF },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_get_Count_m52F7503B30AAD48420E4727CAC11AB7C85844580 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_GetEnumerator_m6E354AFF0AAB0E5F794A1D5FE44A3238F2687970 },
	{ (Il2CppRGCTXDataType)2, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Enumerator_tDCD23637407D83E7BA34F7D8555A272FFB90DE0B },
	{ (Il2CppRGCTXDataType)2, (const Il2CppRGCTXDefinitionData *)&g_rgctx_T_t2787678B008E103B08A08145F5388094F34CD6BB },
	{ (Il2CppRGCTXDataType)2, (const Il2CppRGCTXDefinitionData *)&g_rgctx_IDictionary_2_t15F2148A421F838EF638904D3CCB78C1A81D4260 },
	{ (Il2CppRGCTXDataType)3, (const Il2CppRGCTXDefinitionData *)&g_rgctx_Dictionary_2_TryGetValue_m1396B85B87A7D7DC104EA1F3ADA899CFCE322F05 },
};
extern const CustomAttributesCacheGenerator g_BugsnagUnity_Android_AttributeGenerators[];
IL2CPP_EXTERN_C const Il2CppCodeGenModule g_BugsnagUnity_Android_CodeGenModule;
const Il2CppCodeGenModule g_BugsnagUnity_Android_CodeGenModule = 
{
	"BugsnagUnity.Android.dll",
	497,
	s_methodPointers,
	3,
	s_adjustorThunks,
	s_InvokerIndices,
	0,
	NULL,
	4,
	s_rgctxIndices,
	24,
	s_rgctxValues,
	NULL,
	g_BugsnagUnity_Android_AttributeGenerators,
	NULL, // module initializer,
	NULL,
	NULL,
	NULL,
};
