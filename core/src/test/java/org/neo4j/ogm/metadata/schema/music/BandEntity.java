/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata.schema.music;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Band")
public class BandEntity extends ArtistEntity {

    @Relationship("FOUNDED_IN")
    private CountryEntity foundedIn;

    @Relationship("ACTIVE_SINCE")
    private YearEntity activeSince;

    @Relationship("HAS_MEMBER")
    private List<Member> member = new ArrayList<>();

    @RelationshipEntity("HAS_MEMBER")
    public static class Member {
        @Id
        @GeneratedValue
        private Long memberId;

        @StartNode
        private BandEntity band;

        @EndNode
        private SoloArtistEntity artist;
    }
}
