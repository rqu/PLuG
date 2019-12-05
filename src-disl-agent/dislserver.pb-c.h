/* Generated by the protocol buffer compiler.  DO NOT EDIT! */
/* Generated from: dislserver.proto */

#ifndef PROTOBUF_C_dislserver_2eproto__INCLUDED
#define PROTOBUF_C_dislserver_2eproto__INCLUDED

#include "protobuf-c.h"

PROTOBUF_C__BEGIN_DECLS

#if PROTOBUF_C_VERSION_NUMBER < 1003000
# error This file was generated by a newer version of protoc-c which is incompatible with your libprotobuf-c headers. Please update your headers.
#elif 1003002 < PROTOBUF_C_MIN_COMPILER_VERSION
# error This file was generated by an older version of protoc-c which is incompatible with your libprotobuf-c headers. Please regenerate this file with a newer version of protoc-c.
#endif


typedef struct _InstrumentClassRequest InstrumentClassRequest;
typedef struct _InstrumentClassResponse InstrumentClassResponse;


/* --- enums --- */

typedef enum _InstrumentClassResult {
  INSTRUMENT_CLASS_RESULT__CLASS_UNMODIFIED = 0,
  INSTRUMENT_CLASS_RESULT__CLASS_MODIFIED = 1,
  INSTRUMENT_CLASS_RESULT__ERROR = 3
    PROTOBUF_C__FORCE_ENUM_TO_BE_INT_SIZE(INSTRUMENT_CLASS_RESULT)
} InstrumentClassResult;

/* --- messages --- */

struct  _InstrumentClassRequest
{
  ProtobufCMessage base;
  int32_t flags;
  char *classname;
  ProtobufCBinaryData classbytes;
};
#define INSTRUMENT_CLASS_REQUEST__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&instrument_class_request__descriptor) \
    , 0, (char *)protobuf_c_empty_string, {0,NULL} }


struct  _InstrumentClassResponse
{
  ProtobufCMessage base;
  InstrumentClassResult result;
  char *errormessage;
  ProtobufCBinaryData classbytes;
};
#define INSTRUMENT_CLASS_RESPONSE__INIT \
 { PROTOBUF_C_MESSAGE_INIT (&instrument_class_response__descriptor) \
    , INSTRUMENT_CLASS_RESULT__CLASS_UNMODIFIED, (char *)protobuf_c_empty_string, {0,NULL} }


/* InstrumentClassRequest methods */
void   instrument_class_request__init
                     (InstrumentClassRequest         *message);
size_t instrument_class_request__get_packed_size
                     (const InstrumentClassRequest   *message);
size_t instrument_class_request__pack
                     (const InstrumentClassRequest   *message,
                      uint8_t             *out);
size_t instrument_class_request__pack_to_buffer
                     (const InstrumentClassRequest   *message,
                      ProtobufCBuffer     *buffer);
InstrumentClassRequest *
       instrument_class_request__unpack
                     (ProtobufCAllocator  *allocator,
                      size_t               len,
                      const uint8_t       *data);
void   instrument_class_request__free_unpacked
                     (InstrumentClassRequest *message,
                      ProtobufCAllocator *allocator);
/* InstrumentClassResponse methods */
void   instrument_class_response__init
                     (InstrumentClassResponse         *message);
size_t instrument_class_response__get_packed_size
                     (const InstrumentClassResponse   *message);
size_t instrument_class_response__pack
                     (const InstrumentClassResponse   *message,
                      uint8_t             *out);
size_t instrument_class_response__pack_to_buffer
                     (const InstrumentClassResponse   *message,
                      ProtobufCBuffer     *buffer);
InstrumentClassResponse *
       instrument_class_response__unpack
                     (ProtobufCAllocator  *allocator,
                      size_t               len,
                      const uint8_t       *data);
void   instrument_class_response__free_unpacked
                     (InstrumentClassResponse *message,
                      ProtobufCAllocator *allocator);
/* --- per-message closures --- */

typedef void (*InstrumentClassRequest_Closure)
                 (const InstrumentClassRequest *message,
                  void *closure_data);
typedef void (*InstrumentClassResponse_Closure)
                 (const InstrumentClassResponse *message,
                  void *closure_data);

/* --- services --- */


/* --- descriptors --- */

extern const ProtobufCEnumDescriptor    instrument_class_result__descriptor;
extern const ProtobufCMessageDescriptor instrument_class_request__descriptor;
extern const ProtobufCMessageDescriptor instrument_class_response__descriptor;

PROTOBUF_C__END_DECLS


#endif  /* PROTOBUF_C_dislserver_2eproto__INCLUDED */
