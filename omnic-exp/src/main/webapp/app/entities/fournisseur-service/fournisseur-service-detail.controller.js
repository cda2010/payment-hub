(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('FournisseurServiceDetailController', FournisseurServiceDetailController);

    FournisseurServiceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'FournisseurService', 'ServiceTraitement', 'Pays', 'Api'];

    function FournisseurServiceDetailController($scope, $rootScope, $stateParams, previousState, entity, FournisseurService, ServiceTraitement, Pays, Api) {
        var vm = this;

        vm.fournisseurService = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:fournisseurServiceUpdate', function(event, result) {
            vm.fournisseurService = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
