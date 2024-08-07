package pl.kasprzak.dawid.myfirstwords.service.milestones;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.kasprzak.dawid.myfirstwords.exception.ChildNotFoundException;
import pl.kasprzak.dawid.myfirstwords.exception.MilestoneNotFoundException;
import pl.kasprzak.dawid.myfirstwords.exception.ParentNotFoundException;
import pl.kasprzak.dawid.myfirstwords.repository.MilestonesRepository;
import pl.kasprzak.dawid.myfirstwords.repository.dao.MilestoneEntity;
import pl.kasprzak.dawid.myfirstwords.util.AuthorizationHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeleteMilestoneService {

    private final MilestonesRepository milestonesRepository;
    private final AuthorizationHelper authorizationHelper;

    /**
     * Service method for deleting a milestone identified by the given milestone ID for a specific child.
     * This method validates and authorizes the parent using AuthorizationHelper, and if authorized,
     * finds the milestone associated with the given child ID and milestone ID, and deletes it from the repository.
     *
     * @param childId        the ID of the child to whom the milestone belongs.
     * @param milestoneId    the ID of the milestone to be deleted.
     * @param authentication the authentication object containing the parent's credentials.
     * @throws ParentNotFoundException    if the authenticated parent is not found.
     * @throws ChildNotFoundException     if the child with the given ID is not found.
     * @throws AccessDeniedException      if the authenticated parent does not have access to the child.
     * @throws MilestoneNotFoundException if the milestone with the given ID is not found for the specified child.
     */
    public void deleteMilestone(Long childId, Long milestoneId, Authentication authentication) {
        authorizationHelper.validateAndAuthorizeChild(childId, authentication);
        Optional<MilestoneEntity> milestone = milestonesRepository.findByChildIdAndId(childId, milestoneId);
        MilestoneEntity milestoneEntity = milestone.orElseThrow(() -> new MilestoneNotFoundException("Milestone not found"));
        milestonesRepository.delete(milestoneEntity);
    }
}
