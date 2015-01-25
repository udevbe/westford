//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.trinity.wayland.output;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.freedesktop.wayland.server.Display;

import javax.inject.Inject;
import java.io.IOException;

public class ShellService extends AbstractExecutionThreadService {

    private final Display     display;
    private final JobExecutor jobExecutor;

    @Inject
    ShellService(final Display display,
                 final JobExecutor jobExecutor) {
        this.display = display;
        this.jobExecutor = jobExecutor;
    }

    @Override
    protected void startUp() throws IOException {
        this.jobExecutor.start();
    }

    @Override
    protected void run() {
        this.display.initShm();
        this.display.addSocket("wayland-0");
        this.display.run();
    }

    @Override
    protected void shutDown() throws IOException {
        this.display.terminate();
        this.jobExecutor.fireFinishedEvent();
    }
}