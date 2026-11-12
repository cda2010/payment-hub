(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('StructureOrganisationFacturierDetailController', StructureOrganisationFacturierDetailController);

    StructureOrganisationFacturierDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'StructureOrganisationFacturier'];

    function StructureOrganisationFacturierDetailController($scope, $rootScope, $stateParams, previousState, entity, StructureOrganisationFacturier) {
        var vm = this;

        vm.structureOrganisationFacturier = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:structureOrganisationFacturierUpdate', function(event, result) {
            vm.structureOrganisationFacturier = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
