'use strict';

describe('Controller Tests', function() {

    describe('ParametreUtiliateur Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockParametreUtiliateur, MockInterfaceUtilisateur;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockParametreUtiliateur = jasmine.createSpy('MockParametreUtiliateur');
            MockInterfaceUtilisateur = jasmine.createSpy('MockInterfaceUtilisateur');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'ParametreUtiliateur': MockParametreUtiliateur,
                'InterfaceUtilisateur': MockInterfaceUtilisateur
            };
            createController = function() {
                $injector.get('$controller')("ParametreUtiliateurDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:parametreUtiliateurUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
