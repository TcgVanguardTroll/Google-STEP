// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

    private static final int START_OF_DAY = TimeRange.START_OF_DAY;
    private static final int END_OF_DAY = TimeRange.END_OF_DAY;
    private static final TimeRange WHOLE_DAY = TimeRange.WHOLE_DAY;
    private static final Comparator<TimeRange> ORDER_BY_START = TimeRange.ORDER_BY_START;

    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

        // Array of queries represenitng times to meet.
        List<TimeRange> query = new ArrayList<>();
        
        int numberOfEvents = events.size();
        
        int timeOfStart = START_OF_DAY;
        
        int desiredDuration = (int) request.getDuration();
        
        //  Event counter used whilst iterating.
        int eventCounter = 0;
        
        int eventStart = 0;
        int eventEnd = 0;

        // Set of stirngs representing unique employees set to attend this meeting.
        Set<String> attendees = new Set();

        // Collection of string reprenting who is to attend the requedted meeting
        Collection<String> requestAttendees = request.getAttendees();
        
        // Boolean used to indictae whter or not the attendee should be ignored.
        boolean shouldIgnore = true;

        // Check if duration of event is longer than a whole day and if so return empty.
        if ((int)request.getDuration() > WHOLE_DAY.duration()) {
            return query;
        }

        // Check if there are any events happening at all if so return whole day query.
        if (numberOfEvents == 0) {
            // Adds a 24 hour time range to query.
            query.add(TimeRange.WHOLE_DAY);
            return query;
        }
        // Iterating throguh input event collection. 
        for (Event event : events.sort(event.ORDER_BY_START)) {

            // Collection of the pople who are to attend meeting.
            attendees = event.getAttendees();

            //Ignore event only if attendees of this event are not needed in requested meeting
            for (String attendee : attendees) {
                // If this attendee is required
                if (requestAttendees.contains(attendee)) {
                    // You shouldn't ignore this attendee. 
                    shouldIgnore = false;
                    break;
                } 
                // If at the last event.
                if (eventCounter == numberOfEvents-1) {
                    // Add time range of current start time to end of day.
                    query.add(TimeRange.fromStartEnd(timeOfStart, END_OF_DAY, true));
                }
            }
            // if the attendee is mandatory.
            if (!shouldIgnore) {
                
                // Fetch event start time.
                eventStart = event.getWhen().start();
                // Fetch event end time.
                eventEnd = event.getWhen().end();


            //  If the start of the evnt is directly after another or at the start of the day.
            //  set start of the day to the end of the meeting.
                if (eventStart == timeOfStart) {
                    timeOfStart = eventEnd;
                }

                // Check for overlapping/nested events
                if (eventStart < timeOfStart) {
                    if (eventEnd > timeOfStart) {
                        timeOfStart = eventEnd;
                    }
                }

                //Non-overlapping/non-nested events
                if (timeOfStart <= eventStart) {
                    if (eventStart - timeOfStart >= desiredDuration) {
                        query.add(TimeRange.fromStartEnd(timeOfStart, eventStart, false));
                    }
                    timeOfStart = eventEnd;
                }

                // If at the last event and 
                if (eventCounter == numberOfEvents - 1) {
                    if (timeOfStart != END_OF_DAY + 1) {
                        query.add(TimeRange.fromStartEnd(timeOfStart, TimeRange.END_OF_DAY, true));
                    }
                }
            }
            //  Increment event idx by one/
            eventCounter += 1;
            //  Else set the ignore case to true
            shouldIgnore = true;
        }
        //  Return the meetings.
        return query;

    }
}
