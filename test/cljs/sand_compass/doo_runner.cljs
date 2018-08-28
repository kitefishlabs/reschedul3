(ns sand_compass.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sand_compass.core-test]))

(doo-tests 'sand_compass.core-test)

