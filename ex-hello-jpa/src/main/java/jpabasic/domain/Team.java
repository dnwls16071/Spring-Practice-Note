package jpabasic.domain;

import jakarta.persistence.*;
import jpabasic.domain.baseentity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "TEAM")
public class Team extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "team_id")
	private Long id;
	private String name;

	@OneToMany(mappedBy = "team")
	private List<Member> members = new ArrayList<>();
}
