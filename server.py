#!/usr/bin/env python3
from http.server import HTTPServer, BaseHTTPRequestHandler
import cgi
import os

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

class UploadHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != '/upload':
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not Found')
            return
        ctype, pdict = cgi.parse_header(self.headers.get('content-type'))
        if ctype != 'multipart/form-data':
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b'Bad Request')
            return
        pdict['boundary'] = bytes(pdict['boundary'], "utf-8")
        pdict['CONTENT-LENGTH'] = int(self.headers.get('content-length'))
        fields = cgi.parse_multipart(self.rfile, pdict)
        if 'file' not in fields:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b'No file field')
            return
        for filedata in fields['file']:
            filename = "file_" + str(len(os.listdir(UPLOAD_DIR))+1)
            path = os.path.join(UPLOAD_DIR, filename)
            with open(path, 'wb') as f:
                f.write(filedata)
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'Upload successful')

def run(server_class=HTTPServer, handler_class=UploadHandler):
    server_address = ('', 8080)  # listen on all interfaces
    httpd = server_class(server_address, handler_class)
    print("HTTP Server running on port 8080...")
    httpd.serve_forever()

if __name__ == '__main__':
    run()
