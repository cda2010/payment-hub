(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EmetteurDetailController', EmetteurDetailController);

    EmetteurDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Emetteur', 'Personne', 'Operation'];

    function EmetteurDetailController($scope, $rootScope, $stateParams, previousState, entity, Emetteur, Personne, Operation) {
        var vm = this;

        vm.emetteur = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:emetteurUpdate', function(event, result) {
            vm.emetteur = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
