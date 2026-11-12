(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeDetailController', TaxeDetailController);

    TaxeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Taxe', 'UniteOrganisation', 'TypeService', 'Operation'];

    function TaxeDetailController($scope, $rootScope, $stateParams, previousState, entity, Taxe, UniteOrganisation, TypeService, Operation) {
        var vm = this;

        vm.taxe = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:taxeUpdate', function(event, result) {
            vm.taxe = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
