package com.project.self.user.ai.tool;

import com.project.self.user.ai.context.AgentContext;
import com.project.self.user.ai.context.AgentContextHolder;
import com.project.self.user.ai.dto.JobSummary;
import com.project.self.user.ai.enums.PayloadType;
import com.project.self.user.ai.payload.JobsPayload;
import com.project.self.user.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobTools {

    private final JobService jobService;

    @Tool(
            name = "searchJobs",
            description = """
                    Search jobs.
                    
                    Parameters:
                    
                    keyword - job title or keyword
                    
                    city - city name
                    
                    skills - list of required skills
                    
                    Leave any parameter null if unknown.
                    """
    )
    public List<JobSummary> searchJobs(

            String keyword,

            String city,

            List<String> skills

    ) {

        log.info("keyword={}", keyword);
        log.info("city={}", city);
        log.info("skills={}", skills);

        List<JobSummary> jobs = jobService.searchJobs(
                keyword,
                city,
                skills

        );

        AgentContext context = AgentContextHolder.get();

        context.addTool("searchJobs");

        context.addPayload(
                PayloadType.JOBS,
                new JobsPayload(jobs)
        );

        return jobs;
    }

}