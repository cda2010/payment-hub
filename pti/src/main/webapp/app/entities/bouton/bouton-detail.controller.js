(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BoutonDetailController', BoutonDetailController);

    BoutonDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Bouton'];

    function BoutonDetailController($scope, $rootScope, $stateParams, previousState, entity, Bouton) {
        var vm = this;

        vm.bouton = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:boutonUpdate', function(event, result) {
            vm.bouton = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
