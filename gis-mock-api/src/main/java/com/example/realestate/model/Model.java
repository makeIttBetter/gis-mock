// File: src/main/java/com/example/realestate/model/Model.java
package com.example.realestate.model;

import com.example.realestate.annotations.ExportDataType;
import com.example.realestate.annotations.ExportField;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public abstract class Model implements Serializable {
    @Serial
    private static final long serialVersionUID = 7945147474269998569L;

    @ExportField(fieldName = "id", displayName = "ID")
    @Id
    private String id;

    @ExportField(fieldName = "created_at", displayName = "Created at", exportType = ExportDataType.DATETIME)
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ExportField(fieldName = "updated_at", displayName = "Updated at", exportType = ExportDataType.DATETIME)
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
