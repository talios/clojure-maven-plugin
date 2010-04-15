(require 'swank.swank)

(swank.swank/ignore-protocol-version "2009-09-14")

(do
    (swank.swank/start-server (. (java.io.File/createTempFile "swank" ".port") getAbsolutePath) :dont-close true :port 4005))