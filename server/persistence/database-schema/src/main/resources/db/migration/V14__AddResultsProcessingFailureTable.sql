CREATE TABLE results_processing_failure(
   public_id  varchar(12) primary key references results_processing(public_id) ON DELETE CASCADE,
   body       text not null
);