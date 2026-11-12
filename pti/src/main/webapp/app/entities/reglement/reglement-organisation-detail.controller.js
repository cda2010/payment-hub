(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReglementOrganisationDetailController', ReglementOrganisationDetailController);

    ReglementOrganisationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ReglementOrganisation'];

    function ReglementOrganisationDetailController($scope, $rootScope, $stateParams, previousState, entity, ReglementOrganisation) {
        var vm = this;

        vm.reglement = entity;
        vm.previousState = previousState.name;
        console.log('entity');
        console.log(entity);
        var unsubscribe = $rootScope.$on('ptiApp:reglementUpdate', function(event, result) {
            vm.reglement = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
