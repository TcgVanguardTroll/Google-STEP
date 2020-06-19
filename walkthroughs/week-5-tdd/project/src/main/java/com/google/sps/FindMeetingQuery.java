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
        // Integer represenig the duration of a meeting request.
        int desiredDuration = (int) request.getDuration();

        int counter = 0;
        int event_start = 0;
        int event_end = 0;

        // Set of stirngs representing unique employees set to attend this meeting.
        Set<String> event_attendees = null;

        // Collection of string reprenting who is to attend the requedted meeting
        Collection<String> request_attendees = request.getAttendees();
        boolean ignore_event = true;

        // Check if duration of event is longer than a whole day and if so return empty.
        if ((int)request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return query;
        }

        // Check if there are any events happening at all if so return whole day query.
        if (num_events == 0) {
            query.add(TimeRange.WHOLE_DAY);
            return query;
        }

        for (Event e : events) {

            event_attendees = e.getAttendees();

            //Ignore event only if attendees of this event are not needed in requested meeting
            for (String a : event_attendees) {
                if (request_attendees.contains(a)) {
                    ignore_event = false;
                    break;
                } else if (counter == num_events-1) {
                    query.add(TimeRange.fromStartEnd(next_available_start, TimeRange.END_OF_DAY, true));
                }
            }

            if (!ignore_event) {
                event_start = e.getWhen().start();
                event_end = e.getWhen().end();

                if (event_start == TimeRange.START_OF_DAY) {
                    next_available_start = event_end;
                }

                // Check for overlapping/nested events
                if (event_start < next_available_start) {
                    if (event_end > next_available_start) {
                        next_available_start = event_end;
                    }
                }

                //Non-overlapping/non-nested events
                if (next_available_start <= event_start) {
                    if (event_start - next_available_start >= desired_duration) {
                        query.add(TimeRange.fromStartEnd(next_available_start, event_start, false));
                    }
                    next_available_start = event_end;
                }

                // Last event of the day
                if (counter == num_events-1) {
                    if (next_available_start != TimeRange.END_OF_DAY+1) {
                        query.add(TimeRange.fromStartEnd(next_available_start, TimeRange.END_OF_DAY, true));
                    }
                }
            }

            counter += 1;
            ignore_event = true;
        }

        return query;

    }
}
