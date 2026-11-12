angular.module('ptiApp').directive('timelineModal', function() {
    return {
        restrict: 'E',
        scope: {
            operationStatut: '='
        },
        template: `
                <div>
                {{statutOperation}}
                    <ul class="timeline">
                        <li ng-repeat="statutOperation in operationStatut">
                            <div class="timeline-time">
                                <span class="date">{{statutOperation.etapeService}}</span>
                            </div>
                            <div class="timeline-icon">
                                <a href="javascript:;" ng-class="{'bg-success': statutOperation.statutEtape === 'PROCEED', 'bg-danger': statutOperation.statutEtape === 'INIT'}">&nbsp;</a>
                            </div>
                        </li>
                    </ul>
                </div>
            `,
    };   
});
