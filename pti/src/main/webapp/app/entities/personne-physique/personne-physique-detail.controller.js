(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonnePhysiqueDetailController', PersonnePhysiqueDetailController);

    PersonnePhysiqueDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'PersonnePhysique', 'Nationalite'];

    function PersonnePhysiqueDetailController($scope, $rootScope, $stateParams, previousState, entity, PersonnePhysique, Nationalite) {
        var vm = this;

        vm.personnePhysique = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:personnePhysiqueUpdate', function(event, result) {
            vm.personnePhysique = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
