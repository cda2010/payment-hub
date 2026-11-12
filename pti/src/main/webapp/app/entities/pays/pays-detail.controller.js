(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PaysDetailController', PaysDetailController);

    PaysDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Pays', 'Operation', 'FournisseurService', 'Devise'];

    function PaysDetailController($scope, $rootScope, $stateParams, previousState, entity, Pays, Operation, FournisseurService, Devise) {
        var vm = this;

        vm.pays = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:paysUpdate', function(event, result) {
            vm.pays = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
