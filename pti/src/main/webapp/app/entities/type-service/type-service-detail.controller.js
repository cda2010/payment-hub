(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceDetailController', TypeServiceDetailController);

    TypeServiceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeService', 'Operation', 'TableauFrais', 'TableauRepartitionCommission', 'Autorisation', 'Canal'];

    function TypeServiceDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeService, Operation, TableauFrais, TableauRepartitionCommission, Autorisation, Canal) {
        var vm = this;

        vm.typeService = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typeServiceUpdate', function(event, result) {
            vm.typeService = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
