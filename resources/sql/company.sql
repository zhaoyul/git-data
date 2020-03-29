--:name find-company-by-id :? :1
--:doc
SELECT company_id,company_no,company_name,company_url,company_logo,company_intro,company_index_url,ali_private_key,ali_public_key,ali_app_id  FROM t_company WHERE delete_flag='0' and  company_id=:company-id
