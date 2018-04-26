/* Generated by the protocol buffer compiler.  DO NOT EDIT! */
/* Generated from: dislserver.proto */

/* Do not generate deprecated warnings for self */
#ifndef PROTOBUF_C__NO_DEPRECATED
#define PROTOBUF_C__NO_DEPRECATED
#endif

#include "dislserver.pb-c.h"
void   instrument_class_request__init
                     (InstrumentClassRequest         *message)
{
  static InstrumentClassRequest init_value = INSTRUMENT_CLASS_REQUEST__INIT;
  *message = init_value;
}
size_t instrument_class_request__get_packed_size
                     (const InstrumentClassRequest *message)
{
  assert(message->base.descriptor == &instrument_class_request__descriptor);
  return protobuf_c_message_get_packed_size ((const ProtobufCMessage*)(message));
}
size_t instrument_class_request__pack
                     (const InstrumentClassRequest *message,
                      uint8_t       *out)
{
  assert(message->base.descriptor == &instrument_class_request__descriptor);
  return protobuf_c_message_pack ((const ProtobufCMessage*)message, out);
}
size_t instrument_class_request__pack_to_buffer
                     (const InstrumentClassRequest *message,
                      ProtobufCBuffer *buffer)
{
  assert(message->base.descriptor == &instrument_class_request__descriptor);
  return protobuf_c_message_pack_to_buffer ((const ProtobufCMessage*)message, buffer);
}
InstrumentClassRequest *
       instrument_class_request__unpack
                     (ProtobufCAllocator  *allocator,
                      size_t               len,
                      const uint8_t       *data)
{
  return (InstrumentClassRequest *)
     protobuf_c_message_unpack (&instrument_class_request__descriptor,
                                allocator, len, data);
}
void   instrument_class_request__free_unpacked
                     (InstrumentClassRequest *message,
                      ProtobufCAllocator *allocator)
{
  assert(message->base.descriptor == &instrument_class_request__descriptor);
  protobuf_c_message_free_unpacked ((ProtobufCMessage*)message, allocator);
}
void   instrument_class_response__init
                     (InstrumentClassResponse         *message)
{
  static InstrumentClassResponse init_value = INSTRUMENT_CLASS_RESPONSE__INIT;
  *message = init_value;
}
size_t instrument_class_response__get_packed_size
                     (const InstrumentClassResponse *message)
{
  assert(message->base.descriptor == &instrument_class_response__descriptor);
  return protobuf_c_message_get_packed_size ((const ProtobufCMessage*)(message));
}
size_t instrument_class_response__pack
                     (const InstrumentClassResponse *message,
                      uint8_t       *out)
{
  assert(message->base.descriptor == &instrument_class_response__descriptor);
  return protobuf_c_message_pack ((const ProtobufCMessage*)message, out);
}
size_t instrument_class_response__pack_to_buffer
                     (const InstrumentClassResponse *message,
                      ProtobufCBuffer *buffer)
{
  assert(message->base.descriptor == &instrument_class_response__descriptor);
  return protobuf_c_message_pack_to_buffer ((const ProtobufCMessage*)message, buffer);
}
InstrumentClassResponse *
       instrument_class_response__unpack
                     (ProtobufCAllocator  *allocator,
                      size_t               len,
                      const uint8_t       *data)
{
  return (InstrumentClassResponse *)
     protobuf_c_message_unpack (&instrument_class_response__descriptor,
                                allocator, len, data);
}
void   instrument_class_response__free_unpacked
                     (InstrumentClassResponse *message,
                      ProtobufCAllocator *allocator)
{
  assert(message->base.descriptor == &instrument_class_response__descriptor);
  protobuf_c_message_free_unpacked ((ProtobufCMessage*)message, allocator);
}
static const ProtobufCFieldDescriptor instrument_class_request__field_descriptors[3] =
{
  {
    "flags",
    1,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_INT32,
    offsetof(InstrumentClassRequest, has_flags),
    offsetof(InstrumentClassRequest, flags),
    NULL,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
  {
    "className",
    2,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_STRING,
    0,   /* quantifier_offset */
    offsetof(InstrumentClassRequest, classname),
    NULL,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
  {
    "classBytes",
    3,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_BYTES,
    offsetof(InstrumentClassRequest, has_classbytes),
    offsetof(InstrumentClassRequest, classbytes),
    NULL,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
};
static const unsigned instrument_class_request__field_indices_by_name[] = {
  2,   /* field[2] = classBytes */
  1,   /* field[1] = className */
  0,   /* field[0] = flags */
};
static const ProtobufCIntRange instrument_class_request__number_ranges[1 + 1] =
{
  { 1, 0 },
  { 0, 3 }
};
const ProtobufCMessageDescriptor instrument_class_request__descriptor =
{
  PROTOBUF_C__MESSAGE_DESCRIPTOR_MAGIC,
  "InstrumentClassRequest",
  "InstrumentClassRequest",
  "InstrumentClassRequest",
  "",
  sizeof(InstrumentClassRequest),
  3,
  instrument_class_request__field_descriptors,
  instrument_class_request__field_indices_by_name,
  1,  instrument_class_request__number_ranges,
  (ProtobufCMessageInit) instrument_class_request__init,
  NULL,NULL,NULL    /* reserved[123] */
};
static const ProtobufCFieldDescriptor instrument_class_response__field_descriptors[3] =
{
  {
    "result",
    1,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_ENUM,
    offsetof(InstrumentClassResponse, has_result),
    offsetof(InstrumentClassResponse, result),
    &instrument_class_result__descriptor,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
  {
    "errorMessage",
    2,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_STRING,
    0,   /* quantifier_offset */
    offsetof(InstrumentClassResponse, errormessage),
    NULL,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
  {
    "classBytes",
    3,
    PROTOBUF_C_LABEL_OPTIONAL,
    PROTOBUF_C_TYPE_BYTES,
    offsetof(InstrumentClassResponse, has_classbytes),
    offsetof(InstrumentClassResponse, classbytes),
    NULL,
    NULL,
    0,             /* flags */
    0,NULL,NULL    /* reserved1,reserved2, etc */
  },
};
static const unsigned instrument_class_response__field_indices_by_name[] = {
  2,   /* field[2] = classBytes */
  1,   /* field[1] = errorMessage */
  0,   /* field[0] = result */
};
static const ProtobufCIntRange instrument_class_response__number_ranges[1 + 1] =
{
  { 1, 0 },
  { 0, 3 }
};
const ProtobufCMessageDescriptor instrument_class_response__descriptor =
{
  PROTOBUF_C__MESSAGE_DESCRIPTOR_MAGIC,
  "InstrumentClassResponse",
  "InstrumentClassResponse",
  "InstrumentClassResponse",
  "",
  sizeof(InstrumentClassResponse),
  3,
  instrument_class_response__field_descriptors,
  instrument_class_response__field_indices_by_name,
  1,  instrument_class_response__number_ranges,
  (ProtobufCMessageInit) instrument_class_response__init,
  NULL,NULL,NULL    /* reserved[123] */
};
static const ProtobufCEnumValue instrument_class_result__enum_values_by_number[3] =
{
  { "CLASS_UNMODIFIED", "INSTRUMENT_CLASS_RESULT__CLASS_UNMODIFIED", 0 },
  { "CLASS_MODIFIED", "INSTRUMENT_CLASS_RESULT__CLASS_MODIFIED", 1 },
  { "ERROR", "INSTRUMENT_CLASS_RESULT__ERROR", 3 },
};
static const ProtobufCIntRange instrument_class_result__value_ranges[] = {
{0, 0},{3, 2},{0, 3}
};
static const ProtobufCEnumValueIndex instrument_class_result__enum_values_by_name[3] =
{
  { "CLASS_MODIFIED", 1 },
  { "CLASS_UNMODIFIED", 0 },
  { "ERROR", 2 },
};
const ProtobufCEnumDescriptor instrument_class_result__descriptor =
{
  PROTOBUF_C__ENUM_DESCRIPTOR_MAGIC,
  "InstrumentClassResult",
  "InstrumentClassResult",
  "InstrumentClassResult",
  "",
  3,
  instrument_class_result__enum_values_by_number,
  3,
  instrument_class_result__enum_values_by_name,
  2,
  instrument_class_result__value_ranges,
  NULL,NULL,NULL,NULL   /* reserved[1234] */
};
