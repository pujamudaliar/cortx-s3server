
#pragma once

#ifndef __MERO_FE_S3_SERVER_S3_REQUEST_OBJECT_H__
#define __MERO_FE_S3_SERVER_S3_REQUEST_OBJECT_H__

#include <string>
#include <memory>
#include <map>
#include <set>

/* libevhtp */
#include <evhtp.h>

enum class S3HttpVerb {
  HEAD = htp_method_HEAD,
  GET = htp_method_GET,
  PUT = htp_method_PUT,
  DELETE = htp_method_DELETE,
  POST = htp_method_POST
};

extern "C" int consume_header(evhtp_kv_t * kvobj, void * arg);

class S3RequestObject {
  evhtp_request_t *ev_req;
  std::string bucket_name;
  std::string object_name;
  // This info is fetched from auth service.
  std::string user_name;
  std::string user_id;  // Unique
  std::string account_name;
  std::string account_id;  // Unique

public:
  S3RequestObject(evhtp_request_t *req);
  ~S3RequestObject();

  struct event_base* get_evbase() {
    return this->ev_req->htp->evbase;
  }
  S3HttpVerb http_verb();

  const char* c_get_full_path();

private:
  std::map<std::string, std::string> in_headers_copy;
  bool in_headers_copied;
  std::string full_request_body;
public:
  std::map<std::string, std::string> get_in_headers_copy();
  friend int consume_header(evhtp_kv_t * kvobj, void * arg);

  std::string get_header_value(std::string key);
  std::string get_host_header();
  size_t get_content_length();
  std::string& get_full_body_content_as_string();

  bool has_query_param_key(std::string key);

  // xxx Remove this soon
  struct evbuffer* buffer_in() {
    return ev_req->buffer_in;
  }

  void set_out_header_value(std::string key, std::string value);

  // Operation params.
  std::string get_object_uri();

  void set_bucket_name(std::string& name);
  std::string& get_bucket_name();
  void set_object_name(std::string& name);
  std::string& get_object_name();

  void set_user_name(std::string& name);
  std::string& get_user_name();
  void set_user_id(std::string& id);
  std::string& get_user_id();

  void set_account_name(std::string& name);
  std::string& get_account_name();
  void set_account_id(std::string& id);
  std::string& get_account_id();

  // Response Helpers
private:
  struct evbuffer* reply_buffer;

public:
  void send_response(int code, std::string body = "");
  void send_reply_start(int code);
  void send_reply_body(char *data, int length);
  void send_reply_end();
  void respond_unsupported_api();
};

#endif
