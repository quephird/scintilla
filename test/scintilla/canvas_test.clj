(ns scintilla.color-test
  (:require [clojure.test :refer :all]
            [scintilla.color :refer :all]))

(deftest testing-canvas-operations
  (testing "setting the color of a pixel"
    (let [blank-canvas   (make-canvas 10 20)
          red            [1 0 0]
          altered-canvas (write-pixel blank-canvas 2 3 red)]
      (is (= red (read-pixel altered-canvas 2 3))))))
