(ns reschedul2.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [reschedul2.core-test]))

(doo-tests 'reschedul2.core-test)

