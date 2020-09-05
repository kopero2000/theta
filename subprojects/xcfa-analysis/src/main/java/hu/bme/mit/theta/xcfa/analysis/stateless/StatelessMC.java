/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.xcfa.analysis.stateless;

import hu.bme.mit.theta.xcfa.XCFA;

public final class StatelessMC {

    private final XCFA xcfa;

    private StatelessMC(XCFA xcfa) {
        this.xcfa = xcfa;
    }

    private boolean verify() {
        State state = new State(xcfa);
        XCFA.Process.Procedure.Edge edge = state.getOneStep();
        System.out.println(edge);

        return true;
    }

    public static boolean check(XCFA xcfa) {
        final StatelessMC statelessMC = new StatelessMC(xcfa);
        return statelessMC.verify();
    }
}
