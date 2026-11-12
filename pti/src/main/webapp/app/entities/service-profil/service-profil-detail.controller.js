(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ServiceProfilDetailController', ServiceProfilDetailController);

    ServiceProfilDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ServiceProfil', 'TypeService'];

    function ServiceProfilDetailController($scope, $rootScope, $stateParams, previousState, entity, ServiceProfil, TypeService) {
        var vm = this;

        vm.serviceProfil = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:serviceProfilUpdate', function(event, result) {
            vm.serviceProfil = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
