#include "pch-cpp.hpp"

#ifndef _MSC_VER
# include <alloca.h>
#else
# include <malloc.h>
#endif


#include <limits>
#include <stdint.h>



// System.Char[]
struct CharU5BU5D_t7B7FC5BC8091AA3B9CB0B29CDD80B5EE9254AA34;
// System.Runtime.CompilerServices.CompilationRelaxationsAttribute
struct CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF;
// System.Diagnostics.DebuggableAttribute
struct DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B;
// UnityEngine.Serialization.FormerlySerializedAsAttribute
struct FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210;
// UnityEngine.HeaderAttribute
struct HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB;
// UnityEngine.RangeAttribute
struct RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5;
// System.Runtime.CompilerServices.RuntimeCompatibilityAttribute
struct RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80;
// System.String
struct String_t;
// UnityEngine.TooltipAttribute
struct TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B;
// BugsnagUnity.BugsnagBehaviour/EnumFlagsAttribute
struct EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F;
// BugsnagUnity.BugsnagBehaviour/LabelOverride
struct LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE;



IL2CPP_EXTERN_C_BEGIN
IL2CPP_EXTERN_C_END

#ifdef __clang__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Winvalid-offsetof"
#pragma clang diagnostic ignored "-Wunused-variable"
#endif

// System.Object


// System.Attribute
struct Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71  : public RuntimeObject
{
public:

public:
};


// System.String
struct String_t  : public RuntimeObject
{
public:
	// System.Int32 System.String::m_stringLength
	int32_t ___m_stringLength_0;
	// System.Char System.String::m_firstChar
	Il2CppChar ___m_firstChar_1;

public:
	inline static int32_t get_offset_of_m_stringLength_0() { return static_cast<int32_t>(offsetof(String_t, ___m_stringLength_0)); }
	inline int32_t get_m_stringLength_0() const { return ___m_stringLength_0; }
	inline int32_t* get_address_of_m_stringLength_0() { return &___m_stringLength_0; }
	inline void set_m_stringLength_0(int32_t value)
	{
		___m_stringLength_0 = value;
	}

	inline static int32_t get_offset_of_m_firstChar_1() { return static_cast<int32_t>(offsetof(String_t, ___m_firstChar_1)); }
	inline Il2CppChar get_m_firstChar_1() const { return ___m_firstChar_1; }
	inline Il2CppChar* get_address_of_m_firstChar_1() { return &___m_firstChar_1; }
	inline void set_m_firstChar_1(Il2CppChar value)
	{
		___m_firstChar_1 = value;
	}
};

struct String_t_StaticFields
{
public:
	// System.String System.String::Empty
	String_t* ___Empty_5;

public:
	inline static int32_t get_offset_of_Empty_5() { return static_cast<int32_t>(offsetof(String_t_StaticFields, ___Empty_5)); }
	inline String_t* get_Empty_5() const { return ___Empty_5; }
	inline String_t** get_address_of_Empty_5() { return &___Empty_5; }
	inline void set_Empty_5(String_t* value)
	{
		___Empty_5 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___Empty_5), (void*)value);
	}
};


// System.ValueType
struct ValueType_tDBF999C1B75C48C68621878250DBF6CDBCF51E52  : public RuntimeObject
{
public:

public:
};

// Native definition for P/Invoke marshalling of System.ValueType
struct ValueType_tDBF999C1B75C48C68621878250DBF6CDBCF51E52_marshaled_pinvoke
{
};
// Native definition for COM marshalling of System.ValueType
struct ValueType_tDBF999C1B75C48C68621878250DBF6CDBCF51E52_marshaled_com
{
};

// System.Boolean
struct Boolean_t07D1E3F34E4813023D64F584DFF7B34C9D922F37 
{
public:
	// System.Boolean System.Boolean::m_value
	bool ___m_value_0;

public:
	inline static int32_t get_offset_of_m_value_0() { return static_cast<int32_t>(offsetof(Boolean_t07D1E3F34E4813023D64F584DFF7B34C9D922F37, ___m_value_0)); }
	inline bool get_m_value_0() const { return ___m_value_0; }
	inline bool* get_address_of_m_value_0() { return &___m_value_0; }
	inline void set_m_value_0(bool value)
	{
		___m_value_0 = value;
	}
};

struct Boolean_t07D1E3F34E4813023D64F584DFF7B34C9D922F37_StaticFields
{
public:
	// System.String System.Boolean::TrueString
	String_t* ___TrueString_5;
	// System.String System.Boolean::FalseString
	String_t* ___FalseString_6;

public:
	inline static int32_t get_offset_of_TrueString_5() { return static_cast<int32_t>(offsetof(Boolean_t07D1E3F34E4813023D64F584DFF7B34C9D922F37_StaticFields, ___TrueString_5)); }
	inline String_t* get_TrueString_5() const { return ___TrueString_5; }
	inline String_t** get_address_of_TrueString_5() { return &___TrueString_5; }
	inline void set_TrueString_5(String_t* value)
	{
		___TrueString_5 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___TrueString_5), (void*)value);
	}

	inline static int32_t get_offset_of_FalseString_6() { return static_cast<int32_t>(offsetof(Boolean_t07D1E3F34E4813023D64F584DFF7B34C9D922F37_StaticFields, ___FalseString_6)); }
	inline String_t* get_FalseString_6() const { return ___FalseString_6; }
	inline String_t** get_address_of_FalseString_6() { return &___FalseString_6; }
	inline void set_FalseString_6(String_t* value)
	{
		___FalseString_6 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___FalseString_6), (void*)value);
	}
};


// System.Runtime.CompilerServices.CompilationRelaxationsAttribute
struct CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF  : public Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71
{
public:
	// System.Int32 System.Runtime.CompilerServices.CompilationRelaxationsAttribute::m_relaxations
	int32_t ___m_relaxations_0;

public:
	inline static int32_t get_offset_of_m_relaxations_0() { return static_cast<int32_t>(offsetof(CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF, ___m_relaxations_0)); }
	inline int32_t get_m_relaxations_0() const { return ___m_relaxations_0; }
	inline int32_t* get_address_of_m_relaxations_0() { return &___m_relaxations_0; }
	inline void set_m_relaxations_0(int32_t value)
	{
		___m_relaxations_0 = value;
	}
};


// System.Enum
struct Enum_t23B90B40F60E677A8025267341651C94AE079CDA  : public ValueType_tDBF999C1B75C48C68621878250DBF6CDBCF51E52
{
public:

public:
};

struct Enum_t23B90B40F60E677A8025267341651C94AE079CDA_StaticFields
{
public:
	// System.Char[] System.Enum::enumSeperatorCharArray
	CharU5BU5D_t7B7FC5BC8091AA3B9CB0B29CDD80B5EE9254AA34* ___enumSeperatorCharArray_0;

public:
	inline static int32_t get_offset_of_enumSeperatorCharArray_0() { return static_cast<int32_t>(offsetof(Enum_t23B90B40F60E677A8025267341651C94AE079CDA_StaticFields, ___enumSeperatorCharArray_0)); }
	inline CharU5BU5D_t7B7FC5BC8091AA3B9CB0B29CDD80B5EE9254AA34* get_enumSeperatorCharArray_0() const { return ___enumSeperatorCharArray_0; }
	inline CharU5BU5D_t7B7FC5BC8091AA3B9CB0B29CDD80B5EE9254AA34** get_address_of_enumSeperatorCharArray_0() { return &___enumSeperatorCharArray_0; }
	inline void set_enumSeperatorCharArray_0(CharU5BU5D_t7B7FC5BC8091AA3B9CB0B29CDD80B5EE9254AA34* value)
	{
		___enumSeperatorCharArray_0 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___enumSeperatorCharArray_0), (void*)value);
	}
};

// Native definition for P/Invoke marshalling of System.Enum
struct Enum_t23B90B40F60E677A8025267341651C94AE079CDA_marshaled_pinvoke
{
};
// Native definition for COM marshalling of System.Enum
struct Enum_t23B90B40F60E677A8025267341651C94AE079CDA_marshaled_com
{
};

// UnityEngine.Serialization.FormerlySerializedAsAttribute
struct FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210  : public Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71
{
public:
	// System.String UnityEngine.Serialization.FormerlySerializedAsAttribute::m_oldName
	String_t* ___m_oldName_0;

public:
	inline static int32_t get_offset_of_m_oldName_0() { return static_cast<int32_t>(offsetof(FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210, ___m_oldName_0)); }
	inline String_t* get_m_oldName_0() const { return ___m_oldName_0; }
	inline String_t** get_address_of_m_oldName_0() { return &___m_oldName_0; }
	inline void set_m_oldName_0(String_t* value)
	{
		___m_oldName_0 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___m_oldName_0), (void*)value);
	}
};


// UnityEngine.PropertyAttribute
struct PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052  : public Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71
{
public:

public:
};


// System.Runtime.CompilerServices.RuntimeCompatibilityAttribute
struct RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80  : public Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71
{
public:
	// System.Boolean System.Runtime.CompilerServices.RuntimeCompatibilityAttribute::m_wrapNonExceptionThrows
	bool ___m_wrapNonExceptionThrows_0;

public:
	inline static int32_t get_offset_of_m_wrapNonExceptionThrows_0() { return static_cast<int32_t>(offsetof(RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80, ___m_wrapNonExceptionThrows_0)); }
	inline bool get_m_wrapNonExceptionThrows_0() const { return ___m_wrapNonExceptionThrows_0; }
	inline bool* get_address_of_m_wrapNonExceptionThrows_0() { return &___m_wrapNonExceptionThrows_0; }
	inline void set_m_wrapNonExceptionThrows_0(bool value)
	{
		___m_wrapNonExceptionThrows_0 = value;
	}
};


// System.Void
struct Void_t700C6383A2A510C2CF4DD86DABD5CA9FF70ADAC5 
{
public:
	union
	{
		struct
		{
		};
		uint8_t Void_t700C6383A2A510C2CF4DD86DABD5CA9FF70ADAC5__padding[1];
	};

public:
};


// UnityEngine.HeaderAttribute
struct HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB  : public PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052
{
public:
	// System.String UnityEngine.HeaderAttribute::header
	String_t* ___header_0;

public:
	inline static int32_t get_offset_of_header_0() { return static_cast<int32_t>(offsetof(HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB, ___header_0)); }
	inline String_t* get_header_0() const { return ___header_0; }
	inline String_t** get_address_of_header_0() { return &___header_0; }
	inline void set_header_0(String_t* value)
	{
		___header_0 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___header_0), (void*)value);
	}
};


// UnityEngine.RangeAttribute
struct RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5  : public PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052
{
public:
	// System.Single UnityEngine.RangeAttribute::min
	float ___min_0;
	// System.Single UnityEngine.RangeAttribute::max
	float ___max_1;

public:
	inline static int32_t get_offset_of_min_0() { return static_cast<int32_t>(offsetof(RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5, ___min_0)); }
	inline float get_min_0() const { return ___min_0; }
	inline float* get_address_of_min_0() { return &___min_0; }
	inline void set_min_0(float value)
	{
		___min_0 = value;
	}

	inline static int32_t get_offset_of_max_1() { return static_cast<int32_t>(offsetof(RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5, ___max_1)); }
	inline float get_max_1() const { return ___max_1; }
	inline float* get_address_of_max_1() { return &___max_1; }
	inline void set_max_1(float value)
	{
		___max_1 = value;
	}
};


// UnityEngine.TooltipAttribute
struct TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B  : public PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052
{
public:
	// System.String UnityEngine.TooltipAttribute::tooltip
	String_t* ___tooltip_0;

public:
	inline static int32_t get_offset_of_tooltip_0() { return static_cast<int32_t>(offsetof(TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B, ___tooltip_0)); }
	inline String_t* get_tooltip_0() const { return ___tooltip_0; }
	inline String_t** get_address_of_tooltip_0() { return &___tooltip_0; }
	inline void set_tooltip_0(String_t* value)
	{
		___tooltip_0 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___tooltip_0), (void*)value);
	}
};


// BugsnagUnity.BugsnagBehaviour/EnumFlagsAttribute
struct EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F  : public PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052
{
public:

public:
};


// BugsnagUnity.BugsnagBehaviour/LabelOverride
struct LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE  : public PropertyAttribute_t4A352471DF625C56C811E27AC86B7E1CE6444052
{
public:
	// System.String BugsnagUnity.BugsnagBehaviour/LabelOverride::label
	String_t* ___label_0;

public:
	inline static int32_t get_offset_of_label_0() { return static_cast<int32_t>(offsetof(LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE, ___label_0)); }
	inline String_t* get_label_0() const { return ___label_0; }
	inline String_t** get_address_of_label_0() { return &___label_0; }
	inline void set_label_0(String_t* value)
	{
		___label_0 = value;
		Il2CppCodeGenWriteBarrier((void**)(&___label_0), (void*)value);
	}
};


// System.Diagnostics.DebuggableAttribute/DebuggingModes
struct DebuggingModes_t279D5B9C012ABA935887CB73C5A63A1F46AF08A8 
{
public:
	// System.Int32 System.Diagnostics.DebuggableAttribute/DebuggingModes::value__
	int32_t ___value___2;

public:
	inline static int32_t get_offset_of_value___2() { return static_cast<int32_t>(offsetof(DebuggingModes_t279D5B9C012ABA935887CB73C5A63A1F46AF08A8, ___value___2)); }
	inline int32_t get_value___2() const { return ___value___2; }
	inline int32_t* get_address_of_value___2() { return &___value___2; }
	inline void set_value___2(int32_t value)
	{
		___value___2 = value;
	}
};


// System.Diagnostics.DebuggableAttribute
struct DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B  : public Attribute_t037CA9D9F3B742C063DB364D2EEBBF9FC5772C71
{
public:
	// System.Diagnostics.DebuggableAttribute/DebuggingModes System.Diagnostics.DebuggableAttribute::m_debuggingModes
	int32_t ___m_debuggingModes_0;

public:
	inline static int32_t get_offset_of_m_debuggingModes_0() { return static_cast<int32_t>(offsetof(DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B, ___m_debuggingModes_0)); }
	inline int32_t get_m_debuggingModes_0() const { return ___m_debuggingModes_0; }
	inline int32_t* get_address_of_m_debuggingModes_0() { return &___m_debuggingModes_0; }
	inline void set_m_debuggingModes_0(int32_t value)
	{
		___m_debuggingModes_0 = value;
	}
};

#ifdef __clang__
#pragma clang diagnostic pop
#endif



// System.Void System.Runtime.CompilerServices.CompilationRelaxationsAttribute::.ctor(System.Int32)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void CompilationRelaxationsAttribute__ctor_mAC3079EBC4EEAB474EED8208EF95DB39C922333B (CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF * __this, int32_t ___relaxations0, const RuntimeMethod* method);
// System.Void System.Runtime.CompilerServices.RuntimeCompatibilityAttribute::.ctor()
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void RuntimeCompatibilityAttribute__ctor_m551DDF1438CE97A984571949723F30F44CF7317C (RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80 * __this, const RuntimeMethod* method);
// System.Void System.Runtime.CompilerServices.RuntimeCompatibilityAttribute::set_WrapNonExceptionThrows(System.Boolean)
IL2CPP_MANAGED_FORCE_INLINE IL2CPP_METHOD_ATTR void RuntimeCompatibilityAttribute_set_WrapNonExceptionThrows_m8562196F90F3EBCEC23B5708EE0332842883C490_inline (RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80 * __this, bool ___value0, const RuntimeMethod* method);
// System.Void System.Diagnostics.DebuggableAttribute::.ctor(System.Diagnostics.DebuggableAttribute/DebuggingModes)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void DebuggableAttribute__ctor_m7FF445C8435494A4847123A668D889E692E55550 (DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B * __this, int32_t ___modes0, const RuntimeMethod* method);
// System.Void UnityEngine.HeaderAttribute::.ctor(System.String)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void HeaderAttribute__ctor_m601319E0BCE8C44A9E79B2C0ABAAD0FEF46A9F1E (HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB * __this, String_t* ___header0, const RuntimeMethod* method);
// System.Void UnityEngine.TooltipAttribute::.ctor(System.String)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042 (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * __this, String_t* ___tooltip0, const RuntimeMethod* method);
// System.Void UnityEngine.Serialization.FormerlySerializedAsAttribute::.ctor(System.String)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void FormerlySerializedAsAttribute__ctor_m7A9FC6914FCBA79EE12567BEF3B482CAB7D5265D (FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 * __this, String_t* ___oldName0, const RuntimeMethod* method);
// System.Void BugsnagUnity.BugsnagBehaviour/LabelOverride::.ctor(System.String)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void LabelOverride__ctor_mCF3FDBC98B116E4CB4C79BC4C43BC8FBFB3D948C (LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE * __this, String_t* ___label0, const RuntimeMethod* method);
// System.Void UnityEngine.RangeAttribute::.ctor(System.Single,System.Single)
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void RangeAttribute__ctor_mC74D39A9F20DD2A0D4174F05785ABE4F0DAEF000 (RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5 * __this, float ___min0, float ___max1, const RuntimeMethod* method);
// System.Void BugsnagUnity.BugsnagBehaviour/EnumFlagsAttribute::.ctor()
IL2CPP_EXTERN_C IL2CPP_METHOD_ATTR void EnumFlagsAttribute__ctor_m98C1C50BF9A863996FA0793BB69EF417B8643E28 (EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F * __this, const RuntimeMethod* method);
static void AssemblyU2DCSharpU2Dfirstpass_CustomAttributesCacheGenerator(CustomAttributesCache* cache)
{
	{
		CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF * tmp = (CompilationRelaxationsAttribute_t661FDDC06629BDA607A42BD660944F039FE03AFF *)cache->attributes[0];
		CompilationRelaxationsAttribute__ctor_mAC3079EBC4EEAB474EED8208EF95DB39C922333B(tmp, 8LL, NULL);
	}
	{
		RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80 * tmp = (RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80 *)cache->attributes[1];
		RuntimeCompatibilityAttribute__ctor_m551DDF1438CE97A984571949723F30F44CF7317C(tmp, NULL);
		RuntimeCompatibilityAttribute_set_WrapNonExceptionThrows_m8562196F90F3EBCEC23B5708EE0332842883C490_inline(tmp, true, NULL);
	}
	{
		DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B * tmp = (DebuggableAttribute_tA8054EBD0FC7511695D494B690B5771658E3191B *)cache->attributes[2];
		DebuggableAttribute__ctor_m7FF445C8435494A4847123A668D889E692E55550(tmp, 2LL, NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_APIKey(CustomAttributesCache* cache)
{
	{
		HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB * tmp = (HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB *)cache->attributes[0];
		HeaderAttribute__ctor_m601319E0BCE8C44A9E79B2C0ABAAD0FEF46A9F1E(tmp, il2cpp_codegen_string_new_wrapper("\x42\x61\x73\x69\x63\x20\x53\x65\x74\x74\x69\x6E\x67\x73"), NULL);
	}
	{
		TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * tmp = (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B *)cache->attributes[1];
		TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042(tmp, il2cpp_codegen_string_new_wrapper("\x59\x6F\x75\x72\x20\x42\x75\x67\x73\x6E\x61\x67\x20\x41\x50\x49\x20\x4B\x65\x79\x20\x66\x72\x6F\x6D\x20\x74\x68\x65\x20\x70\x72\x6F\x6A\x65\x63\x74\x20\x73\x65\x74\x74\x69\x6E\x67\x73\x20\x70\x61\x67\x65\x2E"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoDetectErrors(CustomAttributesCache* cache)
{
	{
		FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 * tmp = (FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 *)cache->attributes[0];
		FormerlySerializedAsAttribute__ctor_m7A9FC6914FCBA79EE12567BEF3B482CAB7D5265D(tmp, il2cpp_codegen_string_new_wrapper("\x41\x75\x74\x6F\x4E\x6F\x74\x69\x66\x79"), NULL);
	}
	{
		TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * tmp = (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B *)cache->attributes[1];
		TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042(tmp, il2cpp_codegen_string_new_wrapper("\x53\x68\x6F\x75\x6C\x64\x20\x42\x75\x67\x73\x6E\x61\x67\x20\x61\x75\x74\x6F\x6D\x61\x74\x69\x63\x61\x6C\x6C\x79\x20\x73\x65\x6E\x64\x20\x65\x76\x65\x6E\x74\x73\x20\x74\x6F\x20\x74\x68\x65\x20\x64\x61\x73\x68\x62\x6F\x61\x72\x64\x2E"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoDetectAnrs(CustomAttributesCache* cache)
{
	{
		LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE * tmp = (LabelOverride_tA7841E2A4FC0B42E4D0080B5F73CE34D2C0FA8DE *)cache->attributes[0];
		LabelOverride__ctor_mCF3FDBC98B116E4CB4C79BC4C43BC8FBFB3D948C(tmp, il2cpp_codegen_string_new_wrapper("\x41\x75\x74\x6F\x20\x44\x65\x74\x65\x63\x74\x20\x41\x4E\x52\x73"), NULL);
	}
	{
		TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * tmp = (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B *)cache->attributes[1];
		TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042(tmp, il2cpp_codegen_string_new_wrapper("\x53\x68\x6F\x75\x6C\x64\x20\x42\x75\x67\x73\x6E\x61\x67\x20\x61\x75\x74\x6F\x6D\x61\x74\x69\x63\x61\x6C\x6C\x79\x20\x64\x65\x74\x65\x63\x74\x20\x41\x6E\x64\x72\x6F\x69\x64\x20\x6E\x6F\x74\x20\x72\x65\x73\x70\x6F\x6E\x64\x69\x6E\x67\x20\x65\x72\x72\x6F\x72\x73\x2E"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoTrackSessions(CustomAttributesCache* cache)
{
	{
		TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * tmp = (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B *)cache->attributes[0];
		TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042(tmp, il2cpp_codegen_string_new_wrapper("\x53\x68\x6F\x75\x6C\x64\x20\x42\x75\x67\x73\x6E\x61\x67\x20\x61\x75\x74\x6F\x6D\x61\x74\x69\x63\x61\x6C\x6C\x79\x20\x63\x6F\x6C\x6C\x65\x63\x74\x20\x64\x61\x74\x61\x20\x61\x62\x6F\x75\x74\x20\x73\x65\x73\x73\x69\x6F\x6E\x73\x2E"), NULL);
	}
	{
		FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 * tmp = (FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 *)cache->attributes[1];
		FormerlySerializedAsAttribute__ctor_m7A9FC6914FCBA79EE12567BEF3B482CAB7D5265D(tmp, il2cpp_codegen_string_new_wrapper("\x41\x75\x74\x6F\x43\x61\x70\x74\x75\x72\x65\x53\x65\x73\x73\x69\x6F\x6E\x73"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_NotifyLogLevel(CustomAttributesCache* cache)
{
	{
		FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 * tmp = (FormerlySerializedAsAttribute_t9505BD2243F1C81AB32EEAF3543A796C2D935210 *)cache->attributes[0];
		FormerlySerializedAsAttribute__ctor_m7A9FC6914FCBA79EE12567BEF3B482CAB7D5265D(tmp, il2cpp_codegen_string_new_wrapper("\x4E\x6F\x74\x69\x66\x79\x4C\x65\x76\x65\x6C"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_SecondsPerUniqueLog(CustomAttributesCache* cache)
{
	{
		HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB * tmp = (HeaderAttribute_t9B431E6BA0524D46406D9C413D6A71CB5F2DD1AB *)cache->attributes[0];
		HeaderAttribute__ctor_m601319E0BCE8C44A9E79B2C0ABAAD0FEF46A9F1E(tmp, il2cpp_codegen_string_new_wrapper("\x41\x64\x76\x61\x6E\x63\x65\x64\x20\x53\x65\x74\x74\x69\x6E\x67\x73"), NULL);
	}
	{
		TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B * tmp = (TooltipAttribute_t503A1598A4E68E91673758F50447D0EDFB95149B *)cache->attributes[1];
		TooltipAttribute__ctor_m1839ACEC1560968A6D0EA55D7EB4535546588042(tmp, il2cpp_codegen_string_new_wrapper("\x54\x68\x65\x20\x6E\x75\x6D\x62\x65\x72\x20\x73\x65\x63\x6F\x6E\x64\x73\x20\x72\x65\x71\x75\x69\x72\x65\x64\x20\x62\x65\x74\x77\x65\x65\x6E\x20\x75\x6E\x69\x71\x75\x65\x20\x55\x6E\x69\x74\x79\x20\x6C\x6F\x67\x73\x20\x74\x68\x61\x74\x20\x42\x75\x67\x73\x6E\x61\x67\x20\x77\x69\x6C\x6C\x20\x63\x6F\x6E\x76\x65\x72\x74\x20\x74\x6F\x20\x62\x72\x65\x61\x64\x63\x72\x75\x6D\x62\x73\x20\x6F\x72\x20\x72\x65\x70\x6F\x72\x74\x20\x61\x73\x20\x65\x72\x72\x6F\x72\x73\x20\x28\x69\x66\x20\x63\x6F\x6E\x66\x69\x67\x75\x72\x65\x64\x29\x2E\x20\x49\x6E\x63\x72\x65\x61\x73\x65\x20\x74\x68\x65\x20\x76\x61\x6C\x75\x65\x20\x74\x6F\x20\x72\x65\x64\x75\x63\x65\x20\x74\x68\x65\x20\x6E\x75\x6D\x62\x65\x72\x20\x6F\x66\x20\x6C\x6F\x67\x73\x20\x73\x65\x6E\x74\x2E"), NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_MaximumBreadcrumbs(CustomAttributesCache* cache)
{
	{
		RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5 * tmp = (RangeAttribute_t14A6532D68168764C15E7CF1FDABCD99CB32D0C5 *)cache->attributes[0];
		RangeAttribute__ctor_mC74D39A9F20DD2A0D4174F05785ABE4F0DAEF000(tmp, 0.0f, 100.0f, NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_EnabledBreadcrumbTypes(CustomAttributesCache* cache)
{
	{
		EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F * tmp = (EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F *)cache->attributes[0];
		EnumFlagsAttribute__ctor_m98C1C50BF9A863996FA0793BB69EF417B8643E28(tmp, NULL);
	}
}
static void BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_EnabledErrorTypes(CustomAttributesCache* cache)
{
	{
		EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F * tmp = (EnumFlagsAttribute_t77F943C25C2A723340378B6975B8F9A9E4BD425F *)cache->attributes[0];
		EnumFlagsAttribute__ctor_m98C1C50BF9A863996FA0793BB69EF417B8643E28(tmp, NULL);
	}
}
IL2CPP_EXTERN_C const CustomAttributesCacheGenerator g_AssemblyU2DCSharpU2Dfirstpass_AttributeGenerators[];
const CustomAttributesCacheGenerator g_AssemblyU2DCSharpU2Dfirstpass_AttributeGenerators[10] = 
{
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_APIKey,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoDetectErrors,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoDetectAnrs,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_AutoTrackSessions,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_NotifyLogLevel,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_SecondsPerUniqueLog,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_MaximumBreadcrumbs,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_EnabledBreadcrumbTypes,
	BugsnagBehaviour_t6449F3F1D04DCA6C759933152D86A1C0D476414F_CustomAttributesCacheGenerator_EnabledErrorTypes,
	AssemblyU2DCSharpU2Dfirstpass_CustomAttributesCacheGenerator,
};
IL2CPP_MANAGED_FORCE_INLINE IL2CPP_METHOD_ATTR void RuntimeCompatibilityAttribute_set_WrapNonExceptionThrows_m8562196F90F3EBCEC23B5708EE0332842883C490_inline (RuntimeCompatibilityAttribute_tFF99AB2963098F9CBCD47A20D9FD3D51C17C1C80 * __this, bool ___value0, const RuntimeMethod* method)
{
	{
		bool L_0 = ___value0;
		__this->set_m_wrapNonExceptionThrows_0(L_0);
		return;
	}
}
