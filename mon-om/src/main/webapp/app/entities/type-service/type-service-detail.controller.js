(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('PaysDetailController', PaysDetailController);

    PaysDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Pays', 'FournisseurService'];

    function PaysDetailController($scope, $rootScope, $stateParams, previousState, entity, Pays, FournisseurService) {
        var vm = this;

        vm.pays = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:paysUpdate', function(event, result) {
            vm.pays = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
