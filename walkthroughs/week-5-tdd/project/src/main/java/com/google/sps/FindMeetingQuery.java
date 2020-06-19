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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public final class FindMeetingQuery {

    public static final int START_OF_DAY = TimeRange.START_OF_DAY;
    public static final int END_OF_DAY = TimeRange.END_OF_DAY;
    public static final TimeRange WHOLE_DAY = TimeRange.WHOLE_DAY;
    public static final Comparator<TimeRange> ORDER_BY_START = TimeRange.ORDER_BY_START;


    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

        // Array of queries represenitng times to meet.
        List<TimeRange> query = new ArrayList<>();
        
        // Integer represenitng the number of events.
        int numberOfEvents = events.size();
        
        // Integer representing the Start of a work day.
        int timeOfStart = START_OF_DAY;

        // Integer representing the End of a work day.
        int timeOfEnd = END_OF_DAY;
        
        // Integer represenig the duration of a meeting request.
        int desiredDuration = (int) request.getDuration();
        
        //  Event counter used whilst iterating.
        int eventCounter = 0;
        
        int event_start = 0;
        int event_end = 0;

        // Set of stirngs representing unique employees set to attend this meeting.
        Set<String> event_attendees = null;

        // Collection of string reprenting who is to attend the requedted meeting
        Collection<String> request_attendees = request.getAttendees();
        
        // Boolean used to indictae whter or not the attendee should be ignored.
        boolean shouldIgnore = true;

        // Check if duration of event is longer than a whole day and if so return empty.
        if ((int)request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return query;
        }

        // Check if there are any events happening at all if so return whole day query.
        if (numberOfEvents == 0) {
            // Adds a 24 hour time range to query.
            query.add(TimeRange.WHOLE_DAY);
            return query;
        }
        // Iterating throguh input event collection. 
        for (Event event : events) {

            // Collection of the pople who are to attend meeting.
            attendees = event.getAttendees();

            //Ignore event only if attendees of this event are not needed in requested meeting
            for (String attendee : attendees) {
                // If this attendee is required
                if (request_attendees.contains(attendee)) {
                    // You shouldn't ignore this attendee. 
                    shouldIgnore = false;
                    break;
                } 
                // If at the last event.
                else if (eventCounter == numberOfEvents-1) {
                    query.add(TimeRange.fromStartEnd(timeOfStart, timeOfEnd, true));
                }
            }
            // if the attendee is mandatory.
            if (!shouldIgnore) {
                
                // Fetch event start time.
                event_start = event.getWhen().start();
                // Fetch event end time.
                event_end = event.getWhen().end();

                if (event_start == timeOfStart) {
                    timeOfStart = event_end;
                }

                // Check for overlapping/nested events
                if (event_start < timeOfStart) {
                    if (event_end > timeOfStart) {
                        timeOfStart = event_end;
                    }
                }

                //Non-overlapping/non-nested events
                if (timeOfStart <= event_start) {
                    if (event_start - timeOfStart >= desiredDuration) {
                        query.add(TimeRange.fromStartEnd(timeOfStart, event_start, false));
                    }
                    timeOfStart = event_end;
                }

                // Last event of the day
                if (eventCounter == numberOfEvents-1) {
                    if (timeOfStart != timeOfEnd + 1) {
                        query.add(TimeRange.fromStartEnd(timeOfStart, TimeRange.END_OF_DAY, true));
                    }
                }
            }

            counter += 1;
            ignore_event = true;
        }

        return query;

    }
}
