#ifndef _MESSAGETYPE_H
#define	_MESSAGETYPE_H

// Messages Types
//  - should be in sync with java server

// closing connection
static const jbyte MSG_CLOSE = 0;
// sending analysis
static const jbyte MSG_ANALYZE = 1;
// sending object free
static const jbyte MSG_OBJ_FREE = 2;
// sending new class
static const jbyte MSG_NEW_CLASS = 3;
// sending class info
static const jbyte MSG_CLASS_INFO = 4;
// sending new string
static const jbyte MSG_NEW_STRING = 5;
// sending registration for analysis method
static const jbyte MSG_REG_ANALYSIS = 6;

#endif	/* _MESSAGETYPE_H */
